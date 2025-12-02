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
    
    init {
        loadStats()
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
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
    }
}

