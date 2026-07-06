package skynetbee.developers.production.NeuralEngine.UIFunctions.Utils.background

object Packagename {

    @Volatile
    private var _pkgnam: String? = null

    val pkgnam: String
        get() = _pkgnam
            ?: throw IllegalStateException("Package name not initialized. Call init() first!")

    fun init(name: String) {
        if (_pkgnam == null) {
            _pkgnam = name
        }
    }

    fun isInitialized(): Boolean {
        return _pkgnam != null
    }
}
