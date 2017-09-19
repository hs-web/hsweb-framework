## 文件管理
统一文件管理。提供上传,下载,秒传等API

## API
上传单个文件:
```bash
    POST: /file/upload
    Content-Disposition: form-data; name="file"; filename="test.zip"
```
返回:
```json
    {
       "result":{"id":"文件id","md5":"文件md5值","name":"test.zip"},
       "status":200
    }
```

上传多个文件:
```bash
    POST: /file/upload-multi
    Content-Disposition: form-data; name="files"; filename="test.zip"
    Content-Disposition: form-data; name="files"; filename="test2.zip"
```
返回:
```json
    {
       "result":[
           {"id":"文件id","md5":"文件md5值","name":"test.zip"},
           {"id":"文件id","md5":"文件md5值","name":"test2.zip"}
       ],"status":200
    }
```
上传静态文件:
```bash
    POST: /file/upload-static
    Content-Disposition: form-data; name="file"; filename="test.zip"
```
返回:
```json
    {
      "result":"文件的访问路径",
      "status":200
    }
```

下载文件
```bash
    GET: /file/download/{idOrMd5}
    GET: /file/download/{idOrMd5}/{fileName}
```

根据md5获取文件信息
```bash
    GET: /file/md5/{md5}
```
如果文件存在则返回:
```json
    {
      "result":{
        "id":"文件id",
        "name":"文件名",
        "md5":"md5值"
        .....
      },
      "status":200
    }
```
否则返回:
```json
    {
      "status":404,
      "message":"...."
    }
```