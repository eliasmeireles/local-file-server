package com.softwareplace.fileserver.service

import com.softwareplace.fileserver.file.exception.FileNotFoundException
import com.softwareplace.fileserver.file.exception.FileStorageException
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class FileStorageService {

    private val fileStorageLocation: Path = Paths.get(System.getProperty("user.home")
            .plus(File.separator)
            .plus(".file")
            .plus(File.separator)
            .plus("media"))
            .toAbsolutePath().normalize()

    init {
        try {
            Files.createDirectories(this.fileStorageLocation)
        } catch (ex: Exception) {
            throw FileStorageException("Could not create the directory where the uploaded files will be stored.", ex)
        }

    }

    fun storeFile(file: MultipartFile): String {
        try {
            var fileName = StringUtils
                    .cleanPath(file.originalFilename!!)
                    .replace(" ", "_")

            fileName = fileName.substring(0, fileName.lastIndexOf('.'))

            val targetLocation = this.fileStorageLocation.resolve(fileName)

            val directory = File(targetLocation.toUri())
            if (!directory.exists()) {
                directory.mkdirs()
            }
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

            return fileName
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        throw FileStorageException("Could not store the file, Please try again!")
    }

    fun loadFileAsResource(fileName: String): Resource {
        try {
            val filePath = this.fileStorageLocation.resolve(fileName).normalize()
            val resource = UrlResource(filePath.toUri())
            return if (resource.exists()) {
                resource
            } else {
                throw FileNotFoundException("File not found $fileName")
            }
        } catch (ex: MalformedURLException) {
            throw FileNotFoundException("File not found $fileName", ex)
        }

    }
}
