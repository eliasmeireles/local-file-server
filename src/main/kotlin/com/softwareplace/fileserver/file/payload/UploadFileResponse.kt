package com.softwareplace.fileserver.file.payload


class UploadFileResponse(
        var fileName: String?,
        var fileDownloadUri: String?,
        var fileType: String?,
        var size: Long,
        var extension: String?
)
