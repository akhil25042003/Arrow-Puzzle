package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.DailyRecordEntity
import com.example.data.GameRepository
import com.example.data.LevelProgressEntity
import com.example.data.PlayerStatsEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GameMode {
    CAMPAIGN,
    ZEN,
    BLITZ,
    DAILY,
    CUSTOM
}

data class GameUiState(
    val gameMode: GameMode = GameMode.CAMPAIGN,
    val currentLevel: PuzzleLevel,
    val currentCells: Map<GridPosition, CellItem> = emptyMap(),
    val flyingArrows: List<FlyingArrowState> = emptyList(),
    val bumpMap: Map<GridPosition, Direction> = emptyMap(),
    val hintedPosition: GridPosition? = null,
    val movesCount: Int = 0,
    val invalidTaps: Int = 0,
    val comboCount: Int = 0,
    val elapsedSeconds: Int = 0,
    val isLevelComplete: Boolean = false,
    val earnedStars: Int = 0,
    val canUndo: Boolean = false,
    // Blitz specific
    val blitzTimeRemaining: Int = 60,
    val blitzScore: Int = 0,
    val blitzLevelsCleared: Int = 0,
    val isBlitzActive: Boolean = false,
    val isBlitzGameOver: Boolean = false,
    // Zen specific
    val zenSize: Int = 5,
    // Daily specific
    val dailyTier: Int = 1,
    val dailyDateString: String = "",
    // Persistence
    val playerStats: PlayerStatsEntity = PlayerStatsEntity(),
    val levelProgressList: List<LevelProgressEntity> = emptyList(),
    val dailyRecords: List<DailyRecordEntity> = emptyList(),
    // Settings
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val themeIndex: Int = 0
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)
    val soundManager = SoundManager(application)

    private val undoStack = mutableListOf<MoveAction>()
    private var timerJob: Job? = null
    private var blitzJob: Job? = null

    private val initialLevel = LevelGenerator.generateCampaignLevel(1)

    private val _uiState = MutableStateFlow(
        GameUiState(
            currentLevel = initialLevel,
            currentCells = initialLevel.initialCells,
            dailyDateString = getTodayDateString()
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        // Observe Room Database Progress & Stats
        viewModelScope.launch {
            repository.allProgress.collect { progress ->
                _uiState.update { it.copy(levelProgressList = progress) }
            }
        }

        viewModelScope.launch {
            repository.playerStats.collect { stats ->
                val s = stats ?: PlayerStatsEntity()
                soundManager.soundEnabled = s.soundEnabled
                soundManager.hapticEnabled = s.hapticEnabled
                _uiState.update {
                    it.copy(
                        playerStats = s,
                        soundEnabled = s.soundEnabled,
                        hapticEnabled = s.hapticEnabled,
                        themeIndex = s.themeIndex
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getDailyRecords(getTodayDateString()).collect { records ->
                _uiState.update { it.copy(dailyRecords = records) }
            }
        }

        startTimer()
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isLevelComplete) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    // --- GAMEPLAY INTERACTIONS ---

    fun onCellClicked(pos: GridPosition) {
        val state = _uiState.value
        if (state.isLevelComplete || state.isBlitzGameOver) return

        val cell = state.currentCells[pos] ?: return
        when (cell) {
            is CellItem.Obstacle -> {
                // Tapping an obstacle gives a bump feedback
                triggerBump(pos, Direction.UP)
                soundManager.playBlocked()
            }
            is CellItem.Arrow -> {
                handleArrowClick(pos, cell)
            }
        }
    }

    private fun handleArrowClick(pos: GridPosition, arrow: CellItem.Arrow) {
        val state = _uiState.value
        val dir = arrow.direction
        val isExitClear = checkPathClear(pos, dir, state.currentCells, state.currentLevel)

        if (isExitClear) {
            // Path is clear! Launch the arrow!
            launchArrow(pos, arrow)
        } else {
            // Path is blocked!
            if (arrow.isRotator) {
                // Rotator rotates 90 degrees clockwise
                val newDir = arrow.direction.nextClockwise()
                val updatedArrow = arrow.copy(direction = newDir)
                val newCells = state.currentCells.toMutableMap()
                newCells[pos] = updatedArrow

                undoStack.add(MoveAction.ArrowRotated(pos, arrow.direction, newDir))
                soundManager.playRotate()

                _uiState.update {
                    it.copy(
                        currentCells = newCells,
                        movesCount = it.movesCount + 1,
                        canUndo = undoStack.isNotEmpty(),
                        hintedPosition = null
                    )
                }
            } else {
                // Standard arrow bumps against obstruction
                triggerBump(pos, dir)
                soundManager.playBlocked()
                _uiState.update {
                    it.copy(
                        invalidTaps = it.invalidTaps + 1,
                        comboCount = 0,
                        movesCount = it.movesCount + 1
                    )
                }
            }
        }
    }

    private fun launchArrow(pos: GridPosition, arrow: CellItem.Arrow) {
        val state = _uiState.value
        val newCells = state.currentCells.toMutableMap()
        newCells.remove(pos)

        val newCombo = state.comboCount + 1
        soundManager.playArrowLaunch(newCombo)

        val flying = FlyingArrowState(
            id = arrow.id,
            startRow = pos.row,
            startCol = pos.col,
            direction = arrow.direction,
            colorIndex = arrow.colorIndex,
            isRotator = arrow.isRotator
        )

        undoStack.add(MoveAction.ArrowLaunched(pos, arrow))

        // Check if level is solved (no more arrows left)
        val remainingArrows = newCells.values.count { it is CellItem.Arrow }
        val isSolved = remainingArrows == 0

        var newBlitzScore = state.blitzScore
        var newBlitzTime = state.blitzTimeRemaining

        if (state.gameMode == GameMode.BLITZ && state.isBlitzActive) {
            newBlitzScore += 10 * newCombo
            newBlitzTime = (newBlitzTime + 1).coerceAtMost(99)
        }

        _uiState.update {
            it.copy(
                currentCells = newCells,
                flyingArrows = it.flyingArrows + flying,
                movesCount = it.movesCount + 1,
                comboCount = newCombo,
                canUndo = undoStack.isNotEmpty(),
                hintedPosition = null,
                blitzScore = newBlitzScore,
                blitzTimeRemaining = newBlitzTime
            )
        }

        // Clean up flying arrow after animation completes
        viewModelScope.launch {
            delay(500)
            _uiState.update { cur ->
                cur.copy(flyingArrows = cur.flyingArrows.filter { it.id != arrow.id })
            }
        }

        if (isSolved) {
            onLevelSolved()
        }
    }

    private fun onLevelSolved() {
        val state = _uiState.value
        soundManager.playVictory()

        val par = state.currentLevel.parMoves
        val moves = state.movesCount
        val invalid = state.invalidTaps

        // Compute 3-Star Rating
        val stars = when {
            moves <= par + 1 && invalid == 0 -> 3
            moves <= par + 5 && invalid <= 3 -> 2
            else -> 1
        }

        _uiState.update {
            it.copy(
                isLevelComplete = true,
                earnedStars = stars
            )
        }

        // Save progress to database
        viewModelScope.launch {
            when (state.gameMode) {
                GameMode.CAMPAIGN -> {
                    repository.recordLevelCompleted(
                        levelNumber = state.currentLevel.levelNumber,
                        stars = stars,
                        moves = moves,
                        timeSeconds = state.elapsedSeconds
                    )
                }
                GameMode.DAILY -> {
                    repository.recordDailyCompleted(
                        dateString = state.dailyDateString,
                        tier = state.dailyTier,
                        timeSeconds = state.elapsedSeconds
                    )
                }
                GameMode.BLITZ -> {
                    val nextLevelCleared = state.blitzLevelsCleared + 1
                    val bonusScore = state.blitzScore + 150
                    val bonusTime = (state.blitzTimeRemaining + 10).coerceAtMost(99)
                    repository.updateBlitzScore(bonusScore)
                    _uiState.update {
                        it.copy(
                            blitzLevelsCleared = nextLevelCleared,
                            blitzScore = bonusScore,
                            blitzTimeRemaining = bonusTime
                        )
                    }
                    delay(700)
                    // Automatically transition to next blitz board
                    loadBlitzNextLevel()
                }
                else -> {}
            }
        }
    }

    private fun checkPathClear(
        pos: GridPosition,
        dir: Direction,
        cells: Map<GridPosition, CellItem>,
        level: PuzzleLevel
    ): Boolean {
        var r = pos.row + dir.dr
        var c = pos.col + dir.dc

        while (r in 0 until level.rows && c in 0 until level.cols) {
            val checkPos = GridPosition(r, c)
            val item = cells[checkPos]
            if (item != null) {
                // If there's an arrow or obstacle in front, path is blocked!
                return false
            }
            r += dir.dr
            c += dir.dc
        }
        return true
    }

    private fun triggerBump(pos: GridPosition, dir: Direction) {
        _uiState.update { it.copy(bumpMap = it.bumpMap + (pos to dir)) }
        viewModelScope.launch {
            delay(240)
            _uiState.update { it.copy(bumpMap = it.bumpMap - pos) }
        }
    }

    fun undoMove() {
        if (undoStack.isEmpty() || _uiState.value.isLevelComplete) return

        val lastAction = undoStack.removeAt(undoStack.lastIndex)
        val state = _uiState.value
        val newCells = state.currentCells.toMutableMap()

        when (lastAction) {
            is MoveAction.ArrowLaunched -> {
                newCells[lastAction.position] = lastAction.arrow
            }
            is MoveAction.ArrowRotated -> {
                val current = newCells[lastAction.position]
                if (current is CellItem.Arrow) {
                    newCells[lastAction.position] = current.copy(direction = lastAction.previousDirection)
                }
            }
        }

        soundManager.playUndo()
        _uiState.update {
            it.copy(
                currentCells = newCells,
                movesCount = (it.movesCount - 1).coerceAtLeast(0),
                canUndo = undoStack.isNotEmpty(),
                comboCount = 0,
                hintedPosition = null
            )
        }
    }

    fun requestHint() {
        val state = _uiState.value
        if (state.isLevelComplete) return

        // 1. First priority: find any arrow that can launch RIGHT NOW
        for ((pos, item) in state.currentCells) {
            if (item is CellItem.Arrow) {
                if (checkPathClear(pos, item.direction, state.currentCells, state.currentLevel)) {
                    _uiState.update { it.copy(hintedPosition = pos) }
                    soundManager.playRotate()
                    return
                }
            }
        }

        // 2. Second priority: find any rotator that after 1-3 rotations can launch
        for ((pos, item) in state.currentCells) {
            if (item is CellItem.Arrow && item.isRotator) {
                var testDir = item.direction.nextClockwise()
                repeat(3) {
                    if (checkPathClear(pos, testDir, state.currentCells, state.currentLevel)) {
                        _uiState.update { it.copy(hintedPosition = pos) }
                        soundManager.playRotate()
                        return
                    }
                    testDir = testDir.nextClockwise()
                }
            }
        }

        // 3. Fallback: match first available from solution order
        for (arrowId in state.currentLevel.solutionOrder) {
            for ((pos, item) in state.currentCells) {
                if (item.id == arrowId) {
                    _uiState.update { it.copy(hintedPosition = pos) }
                    soundManager.playRotate()
                    return
                }
            }
        }
    }

    fun restartCurrentLevel() {
        val state = _uiState.value
        undoStack.clear()
        _uiState.update {
            it.copy(
                currentCells = state.currentLevel.initialCells,
                movesCount = 0,
                invalidTaps = 0,
                comboCount = 0,
                elapsedSeconds = 0,
                isLevelComplete = false,
                earnedStars = 0,
                canUndo = false,
                hintedPosition = null
            )
        }
        startTimer()
    }

    // --- NAVIGATION & GAME MODES ---

    fun loadCampaignLevel(levelNum: Long) {
        val target = levelNum.coerceAtLeast(1)
        val level = LevelGenerator.generateCampaignLevel(target)
        undoStack.clear()
        _uiState.update {
            it.copy(
                gameMode = GameMode.CAMPAIGN,
                currentLevel = level,
                currentCells = level.initialCells,
                movesCount = 0,
                invalidTaps = 0,
                comboCount = 0,
                elapsedSeconds = 0,
                isLevelComplete = false,
                earnedStars = 0,
                canUndo = false,
                hintedPosition = null,
                isBlitzActive = false
            )
        }
        startTimer()
    }

    fun nextCampaignLevel() {
        loadCampaignLevel(_uiState.value.currentLevel.levelNumber + 1)
    }

    fun loadZenLevel(size: Int? = null) {
        val s = size ?: _uiState.value.zenSize
        val level = LevelGenerator.generateZenLevel(s)
        undoStack.clear()
        _uiState.update {
            it.copy(
                gameMode = GameMode.ZEN,
                zenSize = s,
                currentLevel = level,
                currentCells = level.initialCells,
                movesCount = 0,
                invalidTaps = 0,
                comboCount = 0,
                elapsedSeconds = 0,
                isLevelComplete = false,
                earnedStars = 0,
                canUndo = false,
                hintedPosition = null,
                isBlitzActive = false
            )
        }
        startTimer()
    }

    fun loadDailyLevel(tier: Int) {
        val dateStr = getTodayDateString()
        val level = LevelGenerator.generateDailyLevel(dateStr, tier)
        undoStack.clear()
        _uiState.update {
            it.copy(
                gameMode = GameMode.DAILY,
                dailyTier = tier,
                dailyDateString = dateStr,
                currentLevel = level,
                currentCells = level.initialCells,
                movesCount = 0,
                invalidTaps = 0,
                comboCount = 0,
                elapsedSeconds = 0,
                isLevelComplete = false,
                earnedStars = 0,
                canUndo = false,
                hintedPosition = null,
                isBlitzActive = false
            )
        }
        startTimer()
    }

    fun loadCustomSeedLevel(seedText: String, size: Int = 5) {
        val level = LevelGenerator.generateFromCustomSeed(seedText, size)
        undoStack.clear()
        _uiState.update {
            it.copy(
                gameMode = GameMode.CUSTOM,
                currentLevel = level,
                currentCells = level.initialCells,
                movesCount = 0,
                invalidTaps = 0,
                comboCount = 0,
                elapsedSeconds = 0,
                isLevelComplete = false,
                earnedStars = 0,
                canUndo = false,
                hintedPosition = null,
                isBlitzActive = false
            )
        }
        startTimer()
    }

    fun startBlitzMode() {
        blitzJob?.cancel()
        val level = LevelGenerator.generateZenLevel(size = 4)
        undoStack.clear()
        _uiState.update {
            it.copy(
                gameMode = GameMode.BLITZ,
                currentLevel = level,
                currentCells = level.initialCells,
                movesCount = 0,
                invalidTaps = 0,
                comboCount = 0,
                elapsedSeconds = 0,
                isLevelComplete = false,
                earnedStars = 0,
                canUndo = false,
                hintedPosition = null,
                blitzTimeRemaining = 60,
                blitzScore = 0,
                blitzLevelsCleared = 0,
                isBlitzActive = true,
                isBlitzGameOver = false
            )
        }

        blitzJob = viewModelScope.launch {
            while (_uiState.value.blitzTimeRemaining > 0 && _uiState.value.isBlitzActive) {
                delay(1000)
                _uiState.update {
                    val remaining = it.blitzTimeRemaining - 1
                    if (remaining <= 0) {
                        it.copy(blitzTimeRemaining = 0, isBlitzGameOver = true, isBlitzActive = false)
                    } else {
                        it.copy(blitzTimeRemaining = remaining)
                    }
                }
            }
        }
    }

    private fun loadBlitzNextLevel() {
        val size = when (_uiState.value.blitzLevelsCleared) {
            in 0..2 -> 4
            in 3..6 -> 5
            else -> 6
        }
        val level = LevelGenerator.generateZenLevel(size = size)
        undoStack.clear()
        _uiState.update {
            it.copy(
                currentLevel = level,
                currentCells = level.initialCells,
                isLevelComplete = false,
                hintedPosition = null
            )
        }
    }

    // --- SETTINGS ---

    fun toggleSound() {
        val newSound = !_uiState.value.soundEnabled
        soundManager.soundEnabled = newSound
        _uiState.update { it.copy(soundEnabled = newSound) }
        viewModelScope.launch {
            repository.updateSettings(newSound, _uiState.value.hapticEnabled, _uiState.value.themeIndex)
        }
    }

    fun toggleHaptic() {
        val newHaptic = !_uiState.value.hapticEnabled
        soundManager.hapticEnabled = newHaptic
        _uiState.update { it.copy(hapticEnabled = newHaptic) }
        viewModelScope.launch {
            repository.updateSettings(_uiState.value.soundEnabled, newHaptic, _uiState.value.themeIndex)
        }
    }

    fun selectTheme(themeIndex: Int) {
        _uiState.update { it.copy(themeIndex = themeIndex) }
        viewModelScope.launch {
            repository.updateSettings(_uiState.value.soundEnabled, _uiState.value.hapticEnabled, themeIndex)
        }
    }
}
