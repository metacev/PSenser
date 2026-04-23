package com.tracemaster.util.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tracemaster.data.local.dao.TrackDao
import com.tracemaster.domain.model.ExportFormat
import com.tracemaster.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

/**
 * 轨迹导出管理器
 * 负责将轨迹导出为各种格式
 */
class ExportManager(private val context: Context, private val trackDao: TrackDao) {
    
    /**
     * 导出轨迹到文件
     * @param trackId 轨迹 ID
     * @param format 导出格式
     * @param outputFile 输出文件
     * @return 导出是否成功
     */
    suspend fun exportTrack(
        trackId: Long,
        format: ExportFormat,
        outputFile: File
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val track = trackDao.getTrackById(trackId)
                ?: return@withContext Result.failure(Exception("轨迹不存在"))
            
            val points = trackDao.getPointsByTrackIdSync(trackId)
            
            outputFile.outputStream().use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    when (format) {
                        ExportFormat.GPX -> GpxExporter.export(track, points, writer)
                        ExportFormat.KML -> KmlExporter.export(track, points, writer)
                        ExportFormat.GEOJSON -> GeoJsonExporter.export(track, points, writer)
                        ExportFormat.CSV -> CsvExporter.export(track, points, writer)
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 导出多条轨迹到单个文件
     * @param trackIds 轨迹 ID 列表
     * @param format 导出格式
     * @param outputFile 输出文件
     * @return 导出是否成功
     */
    suspend fun exportTracks(
        trackIds: List<Long>,
        format: ExportFormat,
        outputFile: File
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val tracksWithData = mutableListOf<Pair<Track, List<TrackPoint>>>()
            
            for (trackId in trackIds) {
                val track = trackDao.getTrackById(trackId)
                if (track != null) {
                    val points = trackDao.getPointsByTrackIdSync(trackId)
                    tracksWithData.add(Pair(track, points))
                }
            }
            
            if (tracksWithData.isEmpty()) {
                return@withContext Result.failure(Exception("没有可导出的轨迹"))
            }
            
            outputFile.outputStream().use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    when (format) {
                        ExportFormat.GPX -> GpxExporter.exportMultiple(tracksWithData, writer)
                        ExportFormat.CSV -> CsvExporter.exportMultiple(tracksWithData, writer)
                        // KML 和 GeoJSON 暂不支持批量导出
                        else -> {
                            if (tracksWithData.isNotEmpty()) {
                                val (track, points) = tracksWithData.first()
                                when (format) {
                                    ExportFormat.KML -> KmlExporter.export(track, points, writer)
                                    ExportFormat.GEOJSON -> GeoJsonExporter.export(track, points, writer)
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取导出文件的 URI
     */
    fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    /**
     * 生成导出文件名
     */
    fun generateFileName(track: Track, format: ExportFormat): String {
        val timestamp = System.currentTimeMillis()
        val safeName = track.name.replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]"), "_")
        return "${safeName}_${timestamp}.${format.extension}"
    }
    
    /**
     * 获取默认导出目录
     */
    fun getDefaultExportDir(): File {
        val dir = File(context.getExternalFilesDir(null), "exports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}
