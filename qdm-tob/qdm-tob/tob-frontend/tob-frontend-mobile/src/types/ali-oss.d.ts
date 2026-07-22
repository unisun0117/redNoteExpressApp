declare module 'ali-oss' {
  interface OssClientOptions {
    region: string
    bucket: string
    accessKeyId: string
    accessKeySecret: string
    stsToken?: string
    refreshSTSToken?: () => Promise<{ accessKeyId: string; accessKeySecret: string; stsToken: string }>
    refreshSTSTokenInterval?: number
  }

  interface UploadCheckpoint {
    doneParts: number
    partCount: number
  }

  interface UploadOptions {
    progress?: (percent: number, checkpoint?: UploadCheckpoint) => void
    partSize?: number
  }

  interface UploadResult {
    url: string
    name: string
  }

  class OSS {
    constructor(options: OssClientOptions)
    multipartUpload(key: string, file: File | string, options?: UploadOptions): Promise<UploadResult>
  }

  export = OSS
}
