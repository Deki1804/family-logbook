package com.familylogbook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.domain.repository.LogbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryCount(
    val category: Category,
    val count: Int
)

data class MoodCount(
    val mood: Mood,
    val count: Int
)

class StatsViewModel(
    private val repository: LogbookRepository
) : ViewModel() {
    
    private val _categoryCounts = MutableStateFlow<List<CategoryCount>>(emptyList())
    val categoryCounts: StateFlow<List<CategoryCount>> = _categoryCounts.asStateFlow()
    
    private val _moodCounts = MutableStateFlow<List<MoodCount>>(emptyList())
    val moodCounts: StateFlow<List<MoodCount>> = _moodCounts.asStateFlow()
    
    private val _temperatureHistory = MutableStateFlow<List<Pair<Long, Float>>>(emptyList())
    val temperatureHistory: StateFlow<List<Pair<Long, Float>>> = _temperatureHistory.asStateFlow()
    
    private val _feedingHistory = MutableStateFlow<List<Pair<Long, Int>>>(emptyList())
    val feedingHistory: StateFlow<List<Pair<Long, Int>>> = _feedingHistory.asStateFlow()
    
    private val _allEntries = MutableStateFlow<List<LogEntry>>(emptyList())
    val allEntries: StateFlow<List<LogEntry>> = _allEntries.asStateFlow()
    
    init {
        loadStats()
    }
    
    fun getEntriesByCategory(category: Category): List<LogEntry> {
        return _allEntries.value.filter { it.category == category }.sortedByDescending { it.timestamp }
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                _allEntries.value = entries
                computeStats(entries)
            }
        }
    }
    
    private fun computeStats(entries: List<LogEntry>) {
        // Count by category
        val categoryMap = entries.groupingBy { it.category }.eachCount()
        _categoryCounts.value = categoryMap.map { (category, count) ->
            CategoryCount(category, count)
        }.sortedByDescending { it.count }
        
        // Count by mood (only entries with mood)
        val moodMap = entries
            .filter { it.mood != null }
            .groupingBy { it.mood!! }
            .eachCount()
        _moodCounts.value = moodMap.map { (mood, count) ->
            MoodCount(mood, count)
        }.sortedByDescending { it.count }
        
        // Temperature history (last 7 days)
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
        _temperatureHistory.value = entries
            .filter { it.temperature != null && it.timestamp >= sevenDaysAgo }
            .sortedBy { it.timestamp }
            .map { Pair(it.timestamp, it.temperature!!) }
        
        // Feeding history (last 7 days, only bottle with amount)
        _feedingHistory.value = entries
            .filter { 
                it.category == Category.FEEDING && 
                it.feedingAmount != null && 
                it.timestamp >= sevenDaysAgo 
            }
            .sortedBy { it.timestamp }
            .map { Pair(it.timestamp, it.feedingAmount!!) }
    }
}

