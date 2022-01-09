package com.softwareplace.fileserver.controller

import com.softwareplace.fileserver.file.payload.UploadFileResponse
import com.softwareplace.fileserver.service.FileStorageService
import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.io.IOException
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(value = ["file"])
@Api(value = "File", tags = ["File"])
class FileController {


    @Autowired
    private lateinit var fileStorageService: FileStorageService


    @PostMapping
    @ApiOperation(value = "Upload de apenas um arquivo!")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<UploadFileResponse> {
        val fileName = fileStorageService.storeFile(file = file)

        val fileDownloadUri = "api/file/$fileName"
        val uploadFileResponse = UploadFileResponse(fileName, fileDownloadUri,
                file.contentType, file.size,
                getFileExtension(file.originalFilename)
        )
        return ResponseEntity(uploadFileResponse, HttpStatus.OK)
    }

    private fun getFileExtension(fileOriginalName: String?): String? {
        return when (fileOriginalName) {
            null -> null
            else -> fileOriginalName.substring(fileOriginalName.lastIndexOf("."))
        }
    }

    @PostMapping(value = ["multiple"])
    @ApiOperation(value = "Upload de uma lista de arquivos!")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "ok", response = List::class)
    ])
    fun uploadMultipleFiles(@RequestParam("files") files: Array<MultipartFile>): ResponseEntity<List<UploadFileResponse>> {
        val filesResponse = arrayListOf<UploadFileResponse>()
        files.forEach {
            try {
                val uploadFileResponse = uploadFile(it).body as UploadFileResponse
                filesResponse.add(uploadFileResponse)
            } catch (ex: ClassCastException) {
                ex.printStackTrace()
            }
        }
        return ResponseEntity(filesResponse, HttpStatus.CREATED)
    }

    @GetMapping(value = ["{fileName:.+}"])
    @ApiOperation(value = "Download de um arquivo informando o seu nome!")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "ok", response = UploadFileResponse::class)
    ])
    fun downloadFile(
            @PathVariable("fileName") fileName: String,
            request: HttpServletRequest): ResponseEntity<Resource> {
        val resource = fileStorageService.loadFileAsResource(fileName = fileName)

        var contentType: String?
        try {
            contentType = request.servletContext.getMimeType(resource.file.absolutePath)
        } catch (ex: IOException) {
            throw RuntimeException("Could not determine file type.")
        }

        if (contentType == null) {
            contentType = "application/octet-stream"
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.filename + "\"")
                .body(resource)
    }

}
