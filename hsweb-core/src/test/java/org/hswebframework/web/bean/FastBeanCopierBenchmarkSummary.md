# FastBeanCopier Benchmark Summary

> 更新时间：2026-10-08

## 2026-10-08：工厂引用与堆内存优化

结论：三项优化通过 126 项行为测试和 184 个 JMH fork 验证。工厂引用不再被共享计划持有，
类型对缓存热查询由 24 B 降至 0 B；默认 ASM 长预热的集合 / 嵌套对象分配减少
21.6% / 22.2%，嵌套对象耗时减少 12.1%。耗时收益并非覆盖全部后端：
旧 `reflect` 的嵌套密集场景长预热增加 7.8%，默认 ASM 类型转换密集场景均值增加 2.0%。

目标：解除静态转换计划对局部 Converter / BeanFactory 的引用，按实际元素数创建默认容器，
并减少复制器缓存命中时的组合键分配。范围为 `hsweb-core`，不改变 BeanUtils 动态注册、
工厂切换、容器类型 / 顺序 / 独立复制、record 和类加载器显式清理契约。

实施：转换计划仅保存静态元数据；容器工厂接收预计元素数；复制器、record 和后端缓存
按源 / 目标 Class 查找，首目标直接存放、少量其他目标用小数组、较多目标用哈希表。
缓存清理按源和目标类加载器同时筛选，避免 ThreadLocal 持有动态类。

验证：先集中运行行为、并发创建和类加载器清理回归，再对照优化前编译快照测量 JMH `B/op`
和耗时，并用线程分配计数与对象尺寸探针比较查询分配、容器容量及稀疏 / 密集缓存占用。
本轮基线已包含上一轮缓存优化，保存在 `hsweb-core/target/jmh-results/heap-optimization/baseline-snapshot/`。

### 引用释放、容器容量与缓存元数据

- `ConversionPlan` 改为静态类，移除外部 support / BeanFactory 引用及重复保存的
  ClassDescription；转换时显式传入当前 support。对象图回归验证局部工厂不可由全局计划到达，
  热计划由另一工厂使用时仍调用新工厂。
- 独立 JVM 中，动态工厂携带的 8 MiB 数组在基线 `clearCache(pluginLoader)` 后仍存活，
  仅全量清理后可回收；优化后按类加载器清理即可回收。WeakReference / GC 探针是补充证据，
  不替代确定性的对象图回归。
- 已知大小的默认 ArrayList、HashSet、LinkedHashSet 和 KeySetView 按元素数创建；
  普通 HashMap / LinkedHashMap 复制及 Collection → Map 不再强制最小 16 桶。
  普通 Map 逐条 `put`，避免 JDK `putAll` 在负载阈值处重新扩大容量。
  任意自定义集合仍使用无参构造器，TreeMap / ConcurrentHashMap 复制保留原分支。
- 线程分配计数探针：1,000,000 次预热后，每轮 5,000,000 次 `getCopier` 命中，
  三轮均由 **24.00 B/查询降至 0.00 B/查询**。冷创建仍有协调对象分配，保留每对类型
  同时创建一次、失败 / null 可重试的行为；工厂在缓存行锁外执行。
- 缓存清理匹配源和目标 Class 的类加载器，并使已经进行中的匹配创建失效，
  允许工厂返回结果但不能将结果重新放入已清理缓存；无关类型对继续保留。

容器反射探针读取 JDK 17 的实际内部容量：

| 容器 / 元素数 | 基线容量 | 优化容量 |
|---|---:|---:|
| ArrayList / 2 | 10 | 2 |
| ArrayList / 100 | 109 | 100 |
| ArrayList / 1000 | 1234 | 1000 |
| LinkedHashMap 复制 / 2 | 4 | 4 |
| LinkedHashMap 复制 / 3 | 8 | 4 |
| Collection → Map / 2 | 16 | 4 |

缓存对象尺寸探针使用 Instrumentation.getObjectSize 汇总从缓存根可达的管理对象，
排除 Class 和共享 value；比较旧的 `ConcurrentHashMap<CacheKey, V>` 与新 `ClassPairCache<V>`，
包含管理包装、空闲创建协调表、桶、行、目标条目等开销，不是整个 copier 或 Class 的 retained heap。

| 源类型数 × 每源目标数 | 基线字节 | 优化字节 | 变化 |
|---|---:|---:|---:|
| 100 × 1 | 6704 | 6872 | +2.5% |
| 100 × 2 | 13328 | 11672 | -12.4% |
| 100 × 8 | 53072 | 28472 | -46.4% |
| 1 × 64 | 4176 | 2976 | -28.7% |
| 100 × 64 | 424016 | 267672 | -36.9% |

此单线程探针每源单目标时新增固定 168 B 管理开销；多目标共享源类型时减少组合键与外层哈希节点。
这些数字是本机压缩引用布局下的管理对象测量，不能直接推算生产总堆内存减少比例。

### 行为与性能验证

- `mvn -o -pl hsweb-core -am test`：126 项测试通过，0 失败、0 错误、0 跳过。
- 新增 11 项覆盖全局计划引用、容器语义、并发发布、跨源 / 同源递归创建、失败重试、
  同名不同类加载器隔离、清理中的创建以及稀疏 / 密集目标缓存。
- 本轮 JMH 整组 160 个 fork 和长预热复测 24 个 fork 均完成，详见下表。

### 本轮 JMH 整组对照

基线是上一轮转换计划 / 默认构造器优化后的编译快照，而不是修复前的 PR HEAD。
同机 Apple M5 Pro / macOS 26.5.2 / Temurin JDK 17.0.18 / JMH 1.37；
两版仅切换实现和测试类目录，benchmark 字节码 SHA-256 同为
`93f830b9b79ade7832ebe1e3f1e3bbdacfa5dc3130a3d15664bbe0be8e541648`。
10 场景 × 4 后端 × 2 版本 × 2 独立 fork，共 160 个 fork，全部完成；
3 × 500ms 预热、5 × 500ms 计时、单线程、512 MiB G1，启用 `gc` profiler，
每后端交错顺序为“基线、优化、优化、基线”。表格是两个 fork 均值。
耗时变化为 `(优化 / 基线 - 1) × 100%`；分配量是 profiler 的 `gc.alloc.rate.norm`。

#### asm-accessor

| 场景 | 基线 µs/op | 优化 µs/op | 耗时变化 | 基线 B/op | 优化 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.0599 | 0.0576 | -3.7% | 72.0 | 56.0 |
| 复杂 Bean → Bean | 1.8294 | 1.9123 | +4.5% | 3324.0 | 3072.0 |
| Bean → Map | 0.2929 | 0.2917 | -0.4% | 1512.0 | 1488.0 |
| 异构 Map → Bean | 2.3119 | 2.3309 | +0.8% | 3824.0 | 3656.0 |
| 类型转换密集 Map → Bean | 2.6970 | 2.7598 | +2.3% | 6096.0 | 5928.0 |
| 集合密集 Map → Bean | 0.4037 | 0.4312 | +6.8% | 408.0 | 320.0 |
| 嵌套密集 Map → Bean | 0.3203 | 0.3124 | -2.5% | 720.0 | 640.0 |
| 仅嵌套对象 Map → Bean | 0.2832 | 0.2749 | -2.9% | 504.0 | 392.0 |
| Bean → Extendable | 0.3758 | 0.3771 | +0.3% | 1456.0 | 1448.0 |
| Extendable → Map | 0.2195 | 0.2242 | +2.2% | 1320.0 | 1296.0 |

#### reflection-accessor

| 场景 | 基线 µs/op | 优化 µs/op | 耗时变化 | 基线 B/op | 优化 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.1450 | 0.1302 | -10.2% | 204.0 | 240.0 |
| 复杂 Bean → Bean | 2.1442 | 2.0478 | -4.5% | 3324.0 | 3096.0 |
| Bean → Map | 0.3260 | 0.3185 | -2.3% | 1512.0 | 1488.0 |
| 异构 Map → Bean | 2.5955 | 2.6483 | +2.0% | 3776.0 | 3656.0 |
| 类型转换密集 Map → Bean | 2.9256 | 2.8835 | -1.4% | 6192.0 | 6024.0 |
| 集合密集 Map → Bean | 0.4388 | 0.4295 | -2.1% | 408.0 | 320.0 |
| 嵌套密集 Map → Bean | 0.3571 | 0.3249 | -9.0% | 720.0 | 640.0 |
| 仅嵌套对象 Map → Bean | 0.3130 | 0.2652 | -15.3% | 496.0 | 392.0 |
| Bean → Extendable | 0.5192 | 0.5226 | +0.7% | 1456.0 | 1448.0 |
| Extendable → Map | 0.2244 | 0.2193 | -2.3% | 1320.0 | 1296.0 |

#### javassist

| 场景 | 基线 µs/op | 优化 µs/op | 耗时变化 | 基线 B/op | 优化 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.0636 | 0.0569 | -10.5% | 104.0 | 80.0 |
| 复杂 Bean → Bean | 1.6579 | 1.6089 | -3.0% | 3256.0 | 2984.0 |
| Bean → Map | 0.3891 | 0.4056 | +4.2% | 1496.0 | 1488.0 |
| 异构 Map → Bean | 2.3907 | 2.2784 | -4.7% | 3816.0 | 3560.0 |
| 类型转换密集 Map → Bean | 2.8269 | 2.6243 | -7.2% | 6184.0 | 6000.0 |
| 集合密集 Map → Bean | 0.4972 | 0.5141 | +3.4% | 520.0 | 432.0 |
| 嵌套密集 Map → Bean | 0.3835 | 0.3530 | -8.0% | 768.0 | 656.0 |
| 仅嵌套对象 Map → Bean | 0.2975 | 0.2453 | -17.6% | 584.0 | 472.0 |
| Bean → Extendable | 0.3022 | 0.3010 | -0.4% | 1456.0 | 1440.0 |
| Extendable → Map | 0.2172 | 0.2103 | -3.2% | 1320.0 | 1296.0 |

#### reflect

| 场景 | 基线 µs/op | 优化 µs/op | 耗时变化 | 基线 B/op | 优化 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.1773 | 0.1703 | -4.0% | 464.0 | 440.0 |
| 复杂 Bean → Bean | 2.3627 | 2.2656 | -4.1% | 4724.0 | 4500.0 |
| Bean → Map | 0.4572 | 0.4435 | -3.0% | 1976.0 | 1952.0 |
| 异构 Map → Bean | 2.7732 | 2.7779 | +0.2% | 4440.0 | 4328.0 |
| 类型转换密集 Map → Bean | 2.9748 | 3.0059 | +1.0% | 6816.0 | 6552.0 |
| 集合密集 Map → Bean | 0.5880 | 0.5729 | -2.6% | 608.0 | 536.0 |
| 嵌套密集 Map → Bean | 0.5369 | 0.5933 | +10.5% | 960.0 | 896.0 |
| 仅嵌套对象 Map → Bean | 0.3914 | 0.3794 | -3.1% | 752.0 | 640.0 |
| Bean → Extendable | 0.6623 | 0.6256 | -5.6% | 1984.0 | 1960.0 |
| Extendable → Map | 0.2426 | 0.2339 | -3.6% | 1368.0 | 1344.0 |

### 本轮长预热复测与边界

对默认 ASM 的集合、复杂 Bean、类型转换密集、嵌套对象四场景，reflection-accessor
简单 Bean 的 fork 间分配差异，以及 reflect 嵌套密集的耗时增加，进行针对性复测。
6 场景 × 2 版本 × 2 独立 fork，共 24 个 fork；每场景 5 × 1s 预热、5 × 1s 计时，
其余 JVM / profiler / 交错顺序与整组相同。

| 后端 / 场景 | 基线 µs/op | 优化 µs/op | 耗时变化 | 基线 B/op | 优化 B/op |
|---|---:|---:|---:|---:|---:|
| ASM / 集合密集 Map → Bean | 0.4058 | 0.4052 | -0.2% | 408.0 | 320.0 |
| ASM / 复杂 Bean → Bean | 1.8484 | 1.8001 | -2.6% | 3336.0 | 3096.0 |
| ASM / 类型转换密集 Map → Bean | 2.6706 | 2.7231 | +2.0% | 6144.0 | 5880.0 |
| ASM / 仅嵌套对象 Map → Bean | 0.2877 | 0.2529 | -12.1% | 504.0 | 392.0 |
| reflection-accessor / 简单 Bean → Bean | 0.1441 | 0.1349 | -6.4% | 264.0 | 240.0 |
| reflect / 嵌套密集 Map → Bean | 0.5520 | 0.5949 | +7.8% | 960.0 | 896.0 |

- 默认 ASM 集合的短窗口 +6.8% 耗时未在长预热中重现，长预热两版基本持平；
  复杂 Bean 的短窗口 +4.5% 也未重现。集合分配持续减少 88 B/op，嵌套对象减少 112 B/op。
- reflection-accessor 简单 Bean 的短窗口基线两个 fork 分配为 264 / 144 B/op，
  导致均值看似由 204 增至 240 B/op；长预热两次基线均为 264，优化均为 240。
  不将短窗口均值的增加作为稳定的分配退化。
- 默认 ASM 类型转换密集长预热基线 fork 为 2.6203 / 2.7209 µs/op，优化为
  2.7394 / 2.7068 µs/op；均值 +2.0%，分配减少 4.3%。两版存在 fork 波动和区间重叠，
  不将此路径描述为提速，也不承诺它没有小幅耗时成本。
- 旧 reflect 嵌套密集的耗时增加在长预热中继续出现：基线两次均为约 0.5520 µs/op，
  优化为 0.5806 / 0.6091 µs/op。分配减少 64 B/op（6.7%），但耗时均值增加 7.8%；
  保留这一测量到的代价，不把它归为已消除的噪声。当前数据无法单独归因于本轮哪个子改动。
- 稀疏缓存有固定管理开销，密集缓存的管理对象节省不等于应用整体 retained heap 节省。
  本轮主要收益是减少局部工厂滞留、临时键分配、容器冗余和扩容数组。
- 基准测量本机单线程热缓存，未覆盖生产并发吞吐、尾延迟或首次缓存创建成本。

原始数据、编译基线、源码与字节码指纹、探针及测试日志位于
`hsweb-core/target/jmh-results/heap-optimization/`（未提交的构建产物）。
`run.py` 重跑整组；`confirm.py` 接受 `backend:scene,scene` 参数，复测选择见 `confirm-cases.json`；
`summary.py` / `summary.py confirm-` 汇总两组。`final-probes.py` 运行分配、容量与缓存尺寸探针，
`retention-probes.py` 运行工厂回收探针。源码与字节码在测量期间保持不变。
已停止的早期容器复制尝试结果位于 `attempt-before-map-sizing/`，未计入上述任何结果。
对应 PR：[hs-web/hsweb-framework#352](https://github.com/hs-web/hsweb-framework/pull/352)。

## 2026-10-08：转换计划与默认构造器缓存优化

结论：优化通过全部行为回归。默认 ASM 的类型转换密集场景整组耗时减少 11.0%、
分配量减少 22.4%；两个嵌套 Map 场景长预热耗时减少 10.4% / 14.4%，
分配量减少 47.5% / 44.1%。收益集中在转换场景，其他路径仍存在 fork 波动，详见下表。

本轮减少热缓存转换的临时对象和默认构造器的重复反射查找，保持动态 BeanUtils 注册、
自定义 BeanFactory、泛型数组隔离及 `clearCache(ClassLoader)` 的原有契约。
范围为 `hsweb-core` 的 `FastBeanCopierConverterSupport`、`FastBeanCopierSupport`；
不改变 Map / 集合复制语义，不缓存注册转换器或自定义工厂创建的对象。

实现：无泛型计划按目标 Class 缓存，有泛型计划仅在缓存未命中时复制数组；
默认构造器元数据按类加载器缓存，并纳入全量 / 指定类加载器清理。

### 基线与验证方式

本轮基线是已经包含 BeanUtils 兼容修复的编译快照，不能用修复前的 PR HEAD 代替。
快照在优化前从当前工作树的 `target/classes`、`target/test-classes` 保存；两版 benchmark
源码 SHA-256 均为 `2371bce59d0845955cc8ea8f4a60d7830400c315f8bee73631f66d5ef21adf3d`，
benchmark 类字节码 SHA-256 均为 `93f830b9b79ade7832ebe1e3f1e3bbdacfa5dc3130a3d15664bbe0be8e541648`。
实现源码指纹见原始结果目录中的 `manifest.json`。

- 同机：Apple M5 Pro，18 核，64 GiB，macOS 26.5.2 / aarch64，Temurin JDK 17.0.18，JMH 1.37。
- 同一依赖 classpath；仅切换前两个条目的实现和测试类目录。
- 每场景单线程、独立 JVM，`-Xms512m -Xmx512m -XX:+UseG1GC`，启用 `gc` profiler。
- 整组：10 场景 × 4 后端 × 2 版本 × 2 独立 fork，共 160 个 fork；
  预热 3 × 500ms、计时 5 × 500ms，每个后端按“基线、优化、优化、基线”交错运行。
- 长预热复测：默认 ASM 的简单 Bean、Bean → Map、两个嵌套 Map 场景，以及短窗口耗时增加的
  Javassist Bean → Extendable、reflect 简单 Bean / Bean → Extendable，共 28 个 fork；
  预热 5 × 1s、计时 5 × 1s，仍使用上述交错顺序。整组及复测共 188 个 fork，全部完成。
- 表格均为两个 fork 均值，耗时单位 `µs/op`、分配量单位 `B/op`；
  耗时变化为 `(优化 / 基线 - 1) × 100%`，负值表示减少。

### 整组结果

#### asm-accessor

| 场景 | 基线 µs/op | 优化 µs/op | 耗时变化 | 基线 B/op | 优化 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.0625 | 0.0617 | -1.2% | 72.0 | 80.0 |
| 复杂 Bean → Bean | 2.0516 | 1.9104 | -6.9% | 4460.0 | 3324.0 |
| Bean → Map | 0.3112 | 0.3023 | -2.9% | 1512.0 | 1512.0 |
| 异构 Map → Bean | 2.6674 | 2.3251 | -12.8% | 5480.0 | 3832.0 |
| 类型转换密集 Map → Bean | 3.0326 | 2.6977 | -11.0% | 7856.0 | 6096.0 |
| 集合密集 Map → Bean | 0.4618 | 0.4145 | -10.2% | 648.0 | 408.0 |
| 嵌套密集 Map → Bean | 0.4227 | 0.3219 | -23.9% | 1280.0 | 688.0 |
| 仅嵌套对象 Map → Bean | 0.3225 | 0.2957 | -8.3% | 960.0 | 504.0 |
| Bean → Extendable | 0.3911 | 0.3820 | -2.3% | 1456.0 | 1456.0 |
| Extendable → Map | 0.2209 | 0.2203 | -0.3% | 1320.0 | 1320.0 |

#### reflection-accessor

| 场景 | 基线 µs/op | 优化 µs/op | 耗时变化 | 基线 B/op | 优化 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.1431 | 0.1437 | +0.5% | 264.0 | 264.0 |
| 复杂 Bean → Bean | 2.2761 | 2.0434 | -10.2% | 4460.0 | 3312.0 |
| Bean → Map | 0.3224 | 0.3218 | -0.2% | 1512.0 | 1512.0 |
| 异构 Map → Bean | 2.8938 | 2.6612 | -8.0% | 5480.0 | 3632.0 |
| 类型转换密集 Map → Bean | 3.1950 | 2.8671 | -10.3% | 7784.0 | 6000.0 |
| 集合密集 Map → Bean | 0.5254 | 0.4406 | -16.1% | 656.0 | 408.0 |
| 嵌套密集 Map → Bean | 0.4185 | 0.3625 | -13.4% | 1288.0 | 752.0 |
| 仅嵌套对象 Map → Bean | 0.3464 | 0.2974 | -14.1% | 960.0 | 504.0 |
| Bean → Extendable | 0.5144 | 0.5160 | +0.3% | 1456.0 | 1456.0 |
| Extendable → Map | 0.2301 | 0.2256 | -1.9% | 1320.0 | 1320.0 |

#### javassist

| 场景 | 基线 µs/op | 优化 µs/op | 耗时变化 | 基线 B/op | 优化 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.0643 | 0.0633 | -1.6% | 104.0 | 104.0 |
| 复杂 Bean → Bean | 1.8606 | 1.6286 | -12.5% | 4632.0 | 3188.0 |
| Bean → Map | 0.5026 | 0.4128 | -17.9% | 2656.0 | 1496.0 |
| 异构 Map → Bean | 2.5142 | 2.3483 | -6.6% | 5776.0 | 3816.0 |
| 类型转换密集 Map → Bean | 2.9243 | 2.6851 | -8.2% | 7968.0 | 6112.0 |
| 集合密集 Map → Bean | 0.5370 | 0.5255 | -2.1% | 928.0 | 520.0 |
| 嵌套密集 Map → Bean | 0.4308 | 0.3918 | -9.1% | 1520.0 | 768.0 |
| 仅嵌套对象 Map → Bean | 0.3183 | 0.2860 | -10.1% | 1064.0 | 584.0 |
| Bean → Extendable | 0.3020 | 0.3158 | +4.5% | 1456.0 | 1456.0 |
| Extendable → Map | 0.2310 | 0.2004 | -13.3% | 1440.0 | 1320.0 |

#### reflect

| 场景 | 基线 µs/op | 优化 µs/op | 耗时变化 | 基线 B/op | 优化 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.1756 | 0.1808 | +3.0% | 464.0 | 464.0 |
| 复杂 Bean → Bean | 2.4072 | 2.3437 | -2.6% | 5772.0 | 4736.0 |
| Bean → Map | 0.4641 | 0.4594 | -1.0% | 1976.0 | 1976.0 |
| 异构 Map → Bean | 3.2424 | 2.7417 | -15.4% | 6112.0 | 4488.0 |
| 类型转换密集 Map → Bean | 3.2432 | 2.9880 | -7.9% | 8632.0 | 6824.0 |
| 集合密集 Map → Bean | 0.6391 | 0.6100 | -4.6% | 856.0 | 608.0 |
| 嵌套密集 Map → Bean | 0.6722 | 0.5679 | -15.5% | 1568.0 | 1008.0 |
| 仅嵌套对象 Map → Bean | 0.4416 | 0.3779 | -14.4% | 1216.0 | 728.0 |
| Bean → Extendable | 0.6192 | 0.6372 | +2.9% | 1984.0 | 1984.0 |
| Extendable → Map | 0.2477 | 0.2346 | -5.3% | 1368.0 | 1368.0 |

### 长预热复测

| 后端 / 场景 | 基线 µs/op | 优化 µs/op | 耗时变化 | 基线 B/op | 优化 B/op |
|---|---:|---:|---:|---:|---:|
| ASM / 简单 Bean → Bean | 0.0626 | 0.0600 | -4.1% | 80.0 | 80.0 |
| ASM / Bean → Map | 0.3009 | 0.3205 | +6.5% | 1512.0 | 1512.0 |
| ASM / 嵌套密集 Map → Bean | 0.3833 | 0.3283 | -14.4% | 1288.0 | 720.0 |
| ASM / 仅嵌套对象 Map → Bean | 0.3219 | 0.2885 | -10.4% | 960.0 | 504.0 |
| Javassist / Bean → Extendable | 0.3032 | 0.3004 | -0.9% | 1456.0 | 1456.0 |
| reflect / 简单 Bean → Bean | 0.1707 | 0.1737 | +1.8% | 464.0 | 464.0 |
| reflect / Bean → Extendable | 0.6186 | 0.6374 | +3.0% | 1984.0 | 1984.0 |

### 结果解释与性能边界

- 无泛型计划命中不再创建 PlanKey；有泛型命中仍使用查找键，但不再复制泛型数组。
  构造器缓存消除重复元数据查找，保留实际反射构造、访问检查和异常语义。
- 默认 ASM 的复杂 Bean、异构 Map、类型转换密集 Map、集合密集 Map 整组分配量
  分别减少约 25.5%、30.1%、22.4%、37.0%；嵌套场景的分配收益在长预热中继续出现。
- 默认 ASM 简单 Bean 短窗口分配均值曾增加 8 B/op，长预热两版均为 80 B/op。
  简单复制的微小耗时差异不作为明确提速收益。
- ASM Bean → Map 长预热均值增加 6.5%，但两次优化 fork 为 `0.3495 / 0.2915 µs/op`，
  基线为 `0.3101 / 0.2917 µs/op`。较高优化 fork 的五个样本为
  `0.3405 / 0.3379 / 0.3784 / 0.3596 / 0.3311 µs/op`，其 JMH 99.9% 误差为 `±0.0743 µs/op`；
  第二次优化 fork 回到基线水平。整组均值反而减少 2.9%，此路径未得到一致的耗时结论。
- Javassist Bean → Extendable 短窗口增加 4.5%，长预热未重现。
  reflect Bean → Extendable 长预热均值仍增加 3.0%；两次基线为 `0.5904 / 0.6469 µs/op`，
  优化为 `0.6361 / 0.6387 µs/op`，落在基线 fork 的范围内。分配量一致，当前数据不能
  确认稳定退化，也不能承诺所有路径零退化；不为消除这类波动特调实现。
- 两个新缓存均纳入 `clearCache()` / `clearCache(ClassLoader)`。自定义工厂实例和
  BeanUtils 注册转换器未被缓存；转换时仍使用当前配置。

### 行为验证与复现

- `mvn -o -pl hsweb-core -am test` 通过：115 项测试，0 失败、0 错误、0 跳过。
- 原有四后端 BeanUtils 扩展回归测试 28 项继续通过。
- 新增 4 项覆盖泛型数组修改后的计划隔离、默认构造器和计划热缓存后工厂替换、
  私有构造器访问检查 / 构造失败异常，以及类加载器清理与重建。
- 原始 JSON、日志、运行脚本、实现指纹、编译基线快照和测试日志位于
  `hsweb-core/target/jmh-results/cache-optimization/`；`run.py` 重跑整组，`confirm.py` 重跑长预热，
  `summary.py` / `summary.py confirm-` 分别汇总两组结果。该目录是未提交的构建产物。
- 所有数据度量本机热缓存微基准，未测生产并发吞吐、尾延迟或首次缓存创建成本。

## 2026-10-08：BeanUtils 转换器兼容修复性能对照

结论：本次修复未观察到可重复的明显耗时退化，但部分场景存在小幅内存分配增加。
默认 `asm-accessor` 的复杂 Bean、类型转换密集 Map 两项整组均值分别增加约 1.0% 和 1.5%；
嵌套 Map 的短窗口高值未在长预热复测中重现。负向变化不作为稳定提速承诺。

### 基线与方法

- 基线：PR #352 分支 `5.0.x-refactor-copy` 的修复前提交 `135b2709a`。
- 修复版：同一提交上的 BeanUtils 转换器兼容修复；本轮测试期间实现源码未变化。
- owning module：`hsweb-core`，实现入口为 `AccessorFastBeanCopierBackend`、`FastBeanCopierConverterSupport`。
- 两边使用同一依赖 classpath、同一份 `FastBeanCopierJmhBenchmark.java`，源码 SHA-256 为
  `2371bce59d0845955cc8ea8f4a60d7830400c315f8bee73631f66d5ef21adf3d`。
- 环境：Apple M5 Pro，18 核，64 GiB 内存，macOS 26.5.2 / aarch64，Temurin JDK 17.0.18，JMH 1.37。
- 每个场景单线程、独立 JVM，`-Xms512m -Xmx512m -XX:+UseG1GC`，启用 `gc` profiler。
- 整组：10 场景 × 4 后端 × 2 版本 × 2 独立 fork，共 160 个 fork。
  每个 fork 预热 3 × 500ms、计时 5 × 500ms；每个后端按“基线、修复、修复、基线”交错运行。
- 复测：默认 ASM 的简单 Bean 及两个嵌套 Map 场景，共 12 个 fork；每个 fork
  预热 5 × 1s、计时 5 × 1s，仍按相同交错顺序运行。
- 下表为两个 fork 均值，单位 `µs/op`，越小越好；变化为 `(修复 / 基线 - 1) × 100%`。
  分配量为 `gc.alloc.rate.norm` 的两次均值，单位 `B/op`。

### 整组结果

#### asm-accessor

| 场景 | 基线 µs/op | 修复 µs/op | 耗时变化 | 基线 B/op | 修复 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.0622 | 0.0602 | -3.2% | 72.0 | 80.0 |
| 复杂 Bean → Bean | 1.9499 | 1.9686 | +1.0% | 4456.0 | 4480.0 |
| Bean → Map | 0.2982 | 0.2927 | -1.8% | 1512.0 | 1512.0 |
| 异构 Map → Bean | 2.4984 | 2.4832 | -0.6% | 5376.0 | 5440.0 |
| 类型转换密集 Map → Bean | 2.9189 | 2.9634 | +1.5% | 7832.0 | 7872.0 |
| 集合密集 Map → Bean | 0.4470 | 0.4358 | -2.5% | 640.0 | 640.0 |
| 嵌套密集 Map → Bean | 0.3673 | 0.3868 | +5.3% | 1208.0 | 1272.0 |
| 仅嵌套对象 Map → Bean | 0.3214 | 0.3684 | +14.6% | 920.0 | 944.0 |
| Bean → Extendable | 0.3871 | 0.3803 | -1.8% | 1456.0 | 1456.0 |
| Extendable → Map | 0.2200 | 0.2212 | +0.5% | 1320.0 | 1320.0 |

#### reflection-accessor

| 场景 | 基线 µs/op | 修复 µs/op | 耗时变化 | 基线 B/op | 修复 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.1406 | 0.1350 | -4.0% | 204.0 | 144.0 |
| 复杂 Bean → Bean | 2.1114 | 2.0945 | -0.8% | 4448.0 | 4456.0 |
| Bean → Map | 0.3133 | 0.3097 | -1.1% | 1512.0 | 1512.0 |
| 异构 Map → Bean | 2.7575 | 2.5774 | -6.5% | 5384.0 | 5456.0 |
| 类型转换密集 Map → Bean | 3.1315 | 3.1086 | -0.7% | 7832.0 | 7888.0 |
| 集合密集 Map → Bean | 0.5068 | 0.4958 | -2.2% | 640.0 | 640.0 |
| 嵌套密集 Map → Bean | 0.4519 | 0.4477 | -0.9% | 1192.0 | 1240.0 |
| 仅嵌套对象 Map → Bean | 0.3317 | 0.3219 | -2.9% | 960.0 | 992.0 |
| Bean → Extendable | 0.5094 | 0.5143 | +1.0% | 1456.0 | 1456.0 |
| Extendable → Map | 0.2222 | 0.2228 | +0.3% | 1320.0 | 1320.0 |

#### javassist

| 场景 | 基线 µs/op | 修复 µs/op | 耗时变化 | 基线 B/op | 修复 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.0725 | 0.0608 | -16.2% | 96.0 | 104.0 |
| 复杂 Bean → Bean | 1.7582 | 1.8063 | +2.7% | 4704.0 | 4776.0 |
| Bean → Map | 0.4983 | 0.5052 | +1.4% | 2656.0 | 2656.0 |
| 异构 Map → Bean | 2.3876 | 2.4522 | +2.7% | 5720.0 | 5664.0 |
| 类型转换密集 Map → Bean | 2.8021 | 2.8118 | +0.3% | 7952.0 | 8080.0 |
| 集合密集 Map → Bean | 0.5232 | 0.5179 | -1.0% | 928.0 | 928.0 |
| 嵌套密集 Map → Bean | 0.3913 | 0.3907 | -0.2% | 1536.0 | 1536.0 |
| 仅嵌套对象 Map → Bean | 0.2863 | 0.2906 | +1.5% | 1184.0 | 1168.0 |
| Bean → Extendable | 0.3023 | 0.3010 | -0.4% | 1456.0 | 1456.0 |
| Extendable → Map | 0.2151 | 0.2129 | -1.0% | 1440.0 | 1440.0 |

#### reflect

| 场景 | 基线 µs/op | 修复 µs/op | 耗时变化 | 基线 B/op | 修复 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.2104 | 0.1881 | -10.6% | 464.0 | 464.0 |
| 复杂 Bean → Bean | 2.6982 | 2.3727 | -12.1% | 5824.0 | 5840.0 |
| Bean → Map | 0.4586 | 0.4593 | +0.2% | 1968.0 | 1968.0 |
| 异构 Map → Bean | 2.9100 | 2.8850 | -0.9% | 6136.0 | 6120.0 |
| 类型转换密集 Map → Bean | 3.2712 | 3.2854 | +0.4% | 8568.0 | 8600.0 |
| 集合密集 Map → Bean | 0.6429 | 0.6253 | -2.7% | 856.0 | 856.0 |
| 嵌套密集 Map → Bean | 0.6344 | 0.6260 | -1.3% | 1512.0 | 1528.0 |
| 仅嵌套对象 Map → Bean | 0.3991 | 0.4040 | +1.2% | 1192.0 | 1176.0 |
| Bean → Extendable | 0.5313 | 0.4823 | -9.2% | 1984.0 | 1976.0 |
| Extendable → Map | 0.2478 | 0.2355 | -5.0% | 1368.0 | 1368.0 |

### 默认 ASM 长预热复测

| 场景 | 基线 µs/op | 修复 µs/op | 耗时变化 | 基线 B/op | 修复 B/op |
|---|---:|---:|---:|---:|---:|
| 简单 Bean → Bean | 0.0771 | 0.0607 | -21.4% | 72.0 | 80.0 |
| 嵌套密集 Map → Bean | 0.3934 | 0.3785 | -3.8% | 1200.0 | 1240.0 |
| 仅嵌套对象 Map → Bean | 0.3691 | 0.3397 | -8.0% | 920.0 | 944.0 |

### 结果解释与边界

- 整组中 ASM 的“仅嵌套对象”均值曾增加 14.6%。其中一次修复版 fork 的五个样本为
  `0.4692 / 0.5720 / 0.3825 / 0.3226 / 0.3222 µs/op`，后两个样本回到首轮水平。
  长预热对照的两次修复版均值为 `0.3497 / 0.3298 µs/op`，没有复现该升高。
  此处存在运行波动，不能把短窗口均值直接认定为持续性能退化。
- 其他后端整组最大正向均值变化为：reflection-accessor 约 1.0%、javassist 约 2.7%、reflect 约 1.2%。
  不对不同 fork 间的下降作稳定优化收益解释。
- 默认 ASM 的复杂转换类场景整组分配量增加约 24～64 B/op，嵌套 Map 的长预热复测
  增加约 24～40 B/op。普通 Bean 复制的分配量也存在 fork 差异，全部数值保留在表格中。
- 这组数据度量同机热缓存微基准，未测生产并发吞吐或尾延迟，也不代表在所有硬件上的绝对性能。
- 注册接口 / 抽象类转换器的原始故障路径在基线中会抛异常，无法进行等价性能比较。
  其正确性由新增 28 项回归测试验证；先前 `hsweb-core` 全量 111 项测试通过的证据仍有效。

### 复现与证据

基线单独工作树：`tmp/worktrees/hsweb-refactor-copy-baseline`。两边直接以 Java classpath
启动 `org.openjdk.jmh.Main`，独立 fork 已正常运行，避开了下方历史记录中 `exec:java` 的 classpath 限制。
使用的 classpath 包括各自 `target/test-classes`、`target/classes` 和完全相同的依赖 JAR。

```bash
java -cp "$JMH_CLASSPATH" org.openjdk.jmh.Main \
  'org.hswebframework.web.bean.FastBeanCopierJmhBenchmark\..*' \
  -p backend=asm-accessor -f 1 -wi 3 -i 5 -w 500ms -r 500ms \
  -t 1 -bm avgt -tu us -jvmArgs '-Xms512m -Xmx512m -XX:+UseG1GC' \
  -prof gc -rf json -rff result.json -foe true
```

对应四种后端分别执行，按上述顺序每个版本运行两次；长预热复测限定三个场景，并将
参数改为 `-wi 5 -i 5 -w 1s -r 1s`。

原始 JSON / 运行日志及汇总保存在 `hsweb-core/target/jmh-results/beanutils-compat/`：

- `comparison.json` / `comparison.csv`：全部整组结果和长预热复测结果。
- `<backend>-<baseline|fixed>-<1|2>.json` / `.log`：整组原始数据及 JVM / 计时配置。
- `confirm-asm-accessor-<baseline|fixed>-<1|2>.json` / `.log`：长预热复测原始数据。
- `run.py` / `confirm.py` / `classpath.txt`：本机实际执行脚本与 classpath。

修复版实现源码 SHA-256：

- `AccessorFastBeanCopierBackend.java`：`c62e0431b9d13a45c9647d3af92b87023e20a62a69bfd11e5c11cd94258b8e20`。
- `FastBeanCopierConverterSupport.java`：`0f091f0f1ab2661e32f91db33876db707c4e5e1d79abb2b1bef37614d6b67d14`。

以下为 2026-05-31 的历史实现与基准记录，本次性能判断以上面的独立 JVM 对照为准。

## 本轮实现摘要

本轮围绕默认 backend、缓存命中稳定性、跨 classloader 健壮性继续收口：

- 默认 backend 切换为 `asm-accessor`
- 增加 `FastBeanCopierBackendSelector`
- 支持通过 `hsweb.fast-bean-copier.backend` 显式指定 backend
- 支持 native-image / disable-codegen 环境探测与 runtime backend 自动降级
- 动态 classloader 场景统一回退到 `reflection-accessor`
- 热点缓存从 weak/soft 策略收口为**强命中优先**
  - volatile copier cache 改为强缓存
  - `ClassDescriptions` / converter 子缓存改为按 classloader 分段的强缓存
  - `GenericKey` 泛型缓存改为强缓存
- 对外暴露 `FastBeanCopier.clearCache()` / `clearCache(ClassLoader)`

## 功能与健壮性验证

单元测试：

```bash
mvn -pl hsweb-core -am -Dtest=FastBeanCopierSupportTest,FastBeanCopierTest -Dsurefire.failIfNoSpecifiedTests=false test
```

当前结果：

- Tests run: `33`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

已覆盖/验证：

- 默认 JVM 环境优先选择 `asm-accessor`
- native hint 环境优先选择 `reflection-accessor`
- backend 显式 override 生效
- native / disable-codegen 下 effective backend 自动降级
- 跨 classloader 复制兼容
- 动态 classloader 使用 volatile cache + reflection fallback
- `clearCache(ClassLoader)` 可回收动态 loader 关联缓存并重建 copier
- 复杂对象 copy、异构 map -> bean、转换密集场景、集合密集场景、嵌套对象场景、extendable 场景兼容

## JMH 验证

执行命令：

```bash
mvn -pl hsweb-core -q \
  -Dexec.classpathScope=test \
  -Dexec.mainClass=org.hswebframework.web.bean.FastBeanCopierJmhRunner \
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

结果文件：

- `hsweb-core/target/jmh-results/fast-bean-copier.json`

说明：

- 当前这条命令在仓库内可以稳定跑通 **非 fork** JMH（当前 Runner 默认不显式设置 forks，沿用 benchmark 的 `@Fork(1)` 会受 `exec-maven-plugin` classpath 影响，见下文“已知限制”）
- 因此当前 JMH 结果适合做**同一进程内相对对比**，不应当作为跨环境绝对值结论

## 代表性结果（当前代码状态）

单位：`us/op`，越小越好。

### simple bean -> bean

- asm-accessor: `0.100`
- javassist: `0.102`
- reflection-accessor: `0.331`
- reflect: `0.573`

### complex bean -> bean

- javassist: `5.547`
- asm-accessor: `6.495`
- reflection-accessor: `8.125`
- reflect: `8.911`

### bean -> map

- asm-accessor: `0.908`
- reflection-accessor: `1.127`
- javassist: `1.851`
- reflect: `2.006`

### heterogeneous map -> bean

- asm-accessor: `7.429`
- reflection-accessor: `7.730`
- javassist: `8.426`
- reflect: `8.953`

### conversion-heavy map -> bean

- asm-accessor: `9.530`
- javassist: `9.785`
- reflection-accessor: `9.974`
- reflect: `11.011`

### collection-heavy map -> bean

- asm-accessor: `0.857`
- reflection-accessor: `0.889`
- javassist: `1.333`
- reflect: `1.530`

### nested-heavy map -> bean

- asm-accessor: `1.177`
- reflection-accessor: `1.241`
- javassist: `1.917`
- reflect: `1.995`

### nested-only map -> bean

- reflection-accessor: `0.811`
- asm-accessor: `0.832`
- javassist: `1.081`
- reflect: `1.254`

### bean -> extendable

- asm-accessor: `1.075`
- javassist: `1.128`
- reflection-accessor: `1.203`
- reflect: `1.511`

### extendable -> map

- reflection-accessor: `1.069`
- asm-accessor: `1.076`
- javassist: `1.128`
- reflect: `1.212`

## 当前结论

### 1. 默认 backend 使用 `asm-accessor` 是合理的

从当前单测内场景 benchmark 与 JMH 一致看：

- `simple bean -> bean`：`asm-accessor` 与 `javassist` 基本持平，略优
- `bean -> map`、`heterogeneous map -> bean`、`conversion-heavy map -> bean`、`collection-heavy map -> bean`、`nested-heavy map -> bean`、`bean -> extendable`：`asm-accessor` 最优或并列最优
- 仅 `complex bean -> bean` 单项上 `javassist` 仍领先，当前约 `1.17x`

综合真实使用面，`asm-accessor` 仍是更合理的默认 backend。

### 2. 当前缓存策略更符合“性能优先”

本轮关键变化不是单纯“多加缓存”，而是把原先容易因 GC 触发重建的缓存收口为：

- 稳定 classloader：强缓存
- 动态 classloader：强缓存命中 + `clearCache(ClassLoader)` 显式释放

这样做的收益：

- 避免 weak/soft value 在内存波动下触发 copier / converter / enum lookup / class description 重建
- 减少热路径吞吐抖动和尾延迟抖动

代价：

- 如果宿主持续创建大量动态 classloader、又不调用 `clearCache(ClassLoader)`，缓存会增长

当前这个取舍更符合 FastBeanCopier 的定位：**优先稳定命中性能**。

### 3. 健壮性整体达标，但“受限反射环境”仍有边界

当前已验证：

1. 普通 JVM：默认 `asm-accessor`
2. native-image / disable-codegen：自动降级到 `reflection-accessor`
3. 跨 classloader：动态 loader 下不再尝试 runtime codegen backend

但仍需注意：

- `FastBeanCopierConverterSupport#createCollectionFactory` 仍使用 `constructor.setAccessible(true)`
- `ReflectionBeanAccessor`、`AsmBeanAccessor` 也仍存在 `setAccessible(true)` / private access 路径

因此：

- **codegen backend 已经规避 native / 动态 loader 风险**
- 但要宣称“完整 native image 适配完成”仍然证据不足
- 当前更准确的结论应是：**native / 受限环境下已有稳定 fallback 基础，但还不是完整 AOT/Native 最终态**

## 已知限制

### 1. forked JMH 仍不可直接用于当前 exec 运行方式

尝试：

```bash
mvn -pl hsweb-core -q \
  -Dexec.classpathScope=test \
  -Dexec.mainClass=org.hswebframework.web.bean.FastBeanCopierJmhRunner \
  -Dhsweb.fast-bean-copier.jmh.include=copySimpleBean \
  -Dhsweb.fast-bean-copier.jmh.forks=1 \
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

会出现：

- `ClassNotFoundException: org.openjdk.jmh.runner.ForkedMain`

因此当前仓库内可信 JMH 证据仍然是：

- `forks=0 / non-forked` 相对对比结果

### 2. 动态 classloader 缓存释放依赖显式清理

这是当前为保证命中性能做的主动取舍。

适合：

- 常规应用
- 少量动态 loader
- 宿主可在卸载插件/脚本/隔离模块时显式 `clearCache(loader)`

不适合：

- 极高频、大量、不可控动态 classloader 且宿主无法感知卸载时机的场景

## 后续优化建议

1. 修复 forked JMH 的 classpath，拿到更强的基准证据
2. 继续减少受限环境下的 `setAccessible(true)` 依赖
3. 若后续确实需要 native image 正式支持，补充：
   - 反射 metadata
   - AOT/static copier SPI
   - 更严格的 native smoke test
