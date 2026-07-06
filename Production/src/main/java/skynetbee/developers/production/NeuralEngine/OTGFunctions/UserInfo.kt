package skynetbee.developers.developerenvironment

/**
 * UserInfo.kt
 * Created by Gowtham Bharath N
 * Date: 23-06-2026
 */

data class FileInfo(
    var name: String? = null,
    var id: String? = null,
    var area: String = "skytest",
    var ownComCode: String = "developer",
    var file: String? = null
)

class User {

    private val fileKeys = listOf(
        "f1","f2","f3","f4","f5","f6","f7","f8","f9","f10","fworkaround"
    )

    private val files = mutableListOf<FileInfo>()

    private var currentFileIndex: Int = 0

    init {
        files.addAll(fileKeys.map { FileInfo() })
    }

    fun setUserInfo(name: String?, id: String?, file: String?) {
        files[currentFileIndex].apply {
            this.name = name
            this.id = id
            this.file = file
        }
    }

    fun getName(): String = files[currentFileIndex].name ?: "Name not set"
    fun getId(): String = files[currentFileIndex].id ?: "ID not set"
    fun getFile(): String = files[currentFileIndex].file ?: "File not set"
    fun getArea(): String = files[currentFileIndex].area
    fun getOwnComCode(): String = files[currentFileIndex].ownComCode
}

var user = User()