package com.utesurdako.openstreetmap.Data

typealias onFieldChangedListener = (field: MapField) -> Unit

class MapField(
    val rows: Int,
    val columns: Int
) {
    private val tiles = Array(rows) { Array(columns) { MapTile() } }

    val listeners = mutableSetOf<onFieldChangedListener>()

    fun getTile(row: Int, column: Int): MapTile {
        if (row < 0 || column < 0 || row >= rows || column >= columns) return MapTile()
        return tiles[row][column]
    }

    fun setTile(row: Int, column: Int, tile: MapTile) {
        if (row < 0 || column < 0 || row >= rows || column >= columns) return
        if (tiles[row][column] != tile) {
            tiles[row][column] = tile
            updateField()
        }
    }

    fun updateField() {
        listeners.forEach {
            it?.invoke(this)
        }
    }
}