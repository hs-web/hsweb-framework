def s_modules = database.getTable("s_modules");

def modules = [
        [u_id: 'api', name: '接口管理', uri: '', icon: '', parent_id: '-1', remark: '', status: 1, optional: '[{"id":"M","text":"菜单可见"}]', sort_index: 6]
        , [u_id: 'oauth2-manager', name: '客户端管理', uri: 'admin/oauth2/list.html', icon: '', parent_id: 'api', remark: '', status: 1, optional: '[{"id":"enable","text":"启用"},{"id":"disable","text":"禁用"},{"id":"M","text":"菜单可见","checked":true},{"id":"R","text":"查询","checked":true},{"id":"C","text":"新增","checked":true},{"id":"U","text":"修改","checked":true},{"id":"D","text":"删除","checked":false}]', sort_index: 601]
];
for (module in modules) {
    s_modules.createInsert().value(module).exec();
}