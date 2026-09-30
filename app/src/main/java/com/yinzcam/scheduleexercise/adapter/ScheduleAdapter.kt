package com.yinzcam.scheduleexercise.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.yinzcam.scheduleexercise.R
import com.yinzcam.scheduleexercise.databinding.ItemByeBinding
import com.yinzcam.scheduleexercise.databinding.ItemGameBinding
import com.yinzcam.scheduleexercise.databinding.ItemScheduleHeaderBinding
import com.yinzcam.scheduleexercise.model.Game
import com.yinzcam.scheduleexercise.model.ScheduleListItem
import com.yinzcam.scheduleexercise.util.GameFormatting
import com.yinzcam.scheduleexercise.util.LogoUrlBuilder

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_GAME = 1
private const val VIEW_TYPE_BYE = 2


class ScheduleAdapter(
    private val onGameClicked: (Game) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ScheduleListItem>()

    fun submitList(newItems: List<ScheduleListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    /** Position of the game matching [gameId], or -1 if not present (used to auto-scroll). */
    fun positionForGameId(gameId: Long): Int {
        return items.indexOfFirst { it is ScheduleListItem.GameRow && it.game.id == gameId }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ScheduleListItem.SectionHeader -> VIEW_TYPE_HEADER
            is ScheduleListItem.GameRow ->
                if (item.game.type == GameFormatting.TYPE_BYE) VIEW_TYPE_BYE else VIEW_TYPE_GAME
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                ItemScheduleHeaderBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_BYE -> ByeViewHolder(
                ItemByeBinding.inflate(inflater, parent, false)
            )
            else -> GameViewHolder(
                ItemGameBinding.inflate(inflater, parent, false),
                onGameClicked
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ScheduleListItem.SectionHeader -> (holder as HeaderViewHolder).bind(item.heading)
            is ScheduleListItem.GameRow -> {
                if (item.game.type == GameFormatting.TYPE_BYE) {
                    (holder as ByeViewHolder).bind(item)
                } else {
                    (holder as GameViewHolder).bind(item)
                }
            }
        }
    }

    private class HeaderViewHolder(
        private val binding: ItemScheduleHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(heading: String) {
            binding.sectionHeaderText.text = heading
        }
    }

    private class ByeViewHolder(
        private val binding: ItemByeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: ScheduleListItem.GameRow) {
            binding.byeWeekText.text = GameFormatting.formatWeekLabel(row.game)
        }
    }

    private class GameViewHolder(
        private val binding: ItemGameBinding,
        private val onGameClicked: (Game) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: ScheduleListItem.GameRow) {
            val game = row.game
            val team = row.team
            val context = binding.root.context

            val opponent = game.opponent

            binding.teamNameText.text = team.name.ifBlank { team.fullName }
            binding.opponentNameText.text = when {
                opponent == null -> ""
                opponent.name.isNotBlank() -> opponent.name
                else -> opponent.fullName
            }

            binding.dateText.text = GameFormatting.formatGameDate(game)
            binding.weekLabelText.text = GameFormatting.formatWeekLabel(game)
            binding.rightLabelText.text = GameFormatting.formatRightLabel(game)

            val network = game.tv
            binding.networkText.visibility = if (network.isBlank()) View.GONE else View.VISIBLE
            binding.networkText.text = network

            binding.vsText.text = if (game.home) "vs" else "@"

            if (GameFormatting.hasScore(game)) {
                binding.teamScoreOrRecordText.text = GameFormatting.homeTeamScore(game)
                binding.opponentScoreOrRecordText.text = GameFormatting.opponentScore(game)
                // Spec item 1: score text size of 32dp (a dimension, applied via COMPLEX_UNIT_PX
                // from the resolved dp value - see dimens.xml for why this is dp, not sp).
                setTextSizeFromDimen(binding.teamScoreOrRecordText, R.dimen.text_score)
                setTextSizeFromDimen(binding.opponentScoreOrRecordText, R.dimen.text_score)
            } else {
                val placeholder = context.getString(R.string.record_placeholder)
                binding.teamScoreOrRecordText.text = team.record.ifBlank { placeholder }
                binding.opponentScoreOrRecordText.text =
                    opponent?.record?.ifBlank { placeholder } ?: placeholder
                // Spec item 7: all other text 14sp.
                setTextSizeFromDimen(binding.teamScoreOrRecordText, R.dimen.text_default)
                setTextSizeFromDimen(binding.opponentScoreOrRecordText, R.dimen.text_default)
            }

            loadLogo(binding.teamLogo, team.triCode)
            loadLogo(binding.opponentLogo, opponent?.triCode.orEmpty())

            binding.root.setOnClickListener { onGameClicked(game) }
        }

        private fun setTextSizeFromDimen(textView: android.widget.TextView, dimenRes: Int) {
            val px = textView.resources.getDimension(dimenRes)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, px)
        }

        private fun loadLogo(imageView: android.widget.ImageView, triCode: String) {
            val url = LogoUrlBuilder.logoUrl(triCode)
            if (url == null) {
                imageView.setImageResource(R.drawable.ic_logo_placeholder)
                return
            }
            Glide.with(imageView)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .placeholder(R.drawable.ic_logo_placeholder)
                .error(R.drawable.ic_logo_placeholder)
                .into(imageView)
        }
    }
}
