object LocationUtils {

    data class LatLng(val x: Double, val y: Double)

    fun parse(value: String?): LatLng? {
        if (value == null) return null
        val coord = value.substringAfter("|", "")
        if (coord.isBlank()) return null

        val (x, y) = coord.split(",")
        return LatLng(x.toDouble(), y.toDouble())
    }
}