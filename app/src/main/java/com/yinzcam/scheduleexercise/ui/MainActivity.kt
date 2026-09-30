package com.yinzcam.scheduleexercise.ui

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.yinzcam.scheduleexercise.R
import com.yinzcam.scheduleexercise.databinding.ActivityMainBinding
import com.yinzcam.scheduleexercise.model.Game
import com.yinzcam.scheduleexercise.adapter.ScheduleAdapter
import com.yinzcam.scheduleexercise.util.GameFormatting

private const val MENU_JUMP_TO_CURRENT = 1
private const val MENU_REFRESH = 2

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ScheduleViewModel by viewModels()

    private lateinit var adapter: ScheduleAdapter

    private var defaultGameId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ScheduleAdapter(onGameClicked = ::showGameDetails)
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.scheduleRecyclerView.adapter = adapter
        binding.scheduleRecyclerView.setHasFixedSize(true)

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadSchedule() }
        binding.retryButton.setOnClickListener { viewModel.loadSchedule() }
        binding.menuButton.setOnClickListener { view -> showAppBarMenu(view) }

        viewModel.uiState.observe(this) { state -> render(state) }
    }

    private fun render(state: ScheduleUiState) {
        when (state) {
            is ScheduleUiState.Loading -> {
                if (!binding.swipeRefresh.isRefreshing) {
                    binding.loadingSpinner.visibility = View.VISIBLE
                }
                binding.errorView.visibility = View.GONE
                binding.swipeRefresh.visibility = View.VISIBLE
            }
            is ScheduleUiState.Success -> {
                binding.loadingSpinner.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                binding.errorView.visibility = View.GONE
                binding.swipeRefresh.visibility = View.VISIBLE
                adapter.submitList(state.items)
                defaultGameId = state.defaultGameId
            }
            is ScheduleUiState.Error -> {
                binding.loadingSpinner.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                binding.swipeRefresh.visibility = View.GONE
                binding.errorView.visibility = View.VISIBLE
                binding.errorMessage.text = state.message
            }
        }
    }


    private fun showAppBarMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(Menu.NONE, MENU_JUMP_TO_CURRENT, 0, R.string.menu_jump_to_current)
        popup.menu.add(Menu.NONE, MENU_REFRESH, 1, R.string.menu_refresh)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_JUMP_TO_CURRENT -> {
                    jumpToCurrentGame()
                    true
                }
                MENU_REFRESH -> {
                    viewModel.loadSchedule()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun jumpToCurrentGame() {
        val position = adapter.positionForGameId(defaultGameId)
        if (position >= 0) {
            binding.scheduleRecyclerView.smoothScrollToPosition(position)
        } else {
            Toast.makeText(this, R.string.no_current_game, Toast.LENGTH_SHORT).show()
        }
    }


    private fun showGameDetails(game: Game) {
        val opponent = game.opponent
        val opponentLabel = when {
            opponent == null -> "TBD"
            opponent.fullName.isNotBlank() -> opponent.fullName
            opponent.name.isNotBlank() -> opponent.name
            else -> "TBD"
        }
        val lines = buildList {
            if (game.venue.isNotBlank()) add("Venue: ${game.venue}")
            if (game.result.isNotBlank()) add("Result: ${game.result}")
            val rightLabel = GameFormatting.formatRightLabel(game)
            if (rightLabel.isNotBlank()) add(
                if (game.type == GameFormatting.TYPE_FINAL) "Final: $rightLabel" else "Kickoff: $rightLabel"
            )
            if (game.tv.isNotBlank()) add("TV: ${game.tv}")
        }

        AlertDialog.Builder(this)
            .setTitle("${GameFormatting.formatWeekLabel(game)} vs $opponentLabel")
            .setMessage(if (lines.isEmpty()) "No additional details." else lines.joinToString("\n"))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
