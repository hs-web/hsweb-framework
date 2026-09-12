# FastBeanCopier Benchmark Summary

> 更新时间：2026-05-31

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
