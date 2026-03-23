package cn.gdeiassistant.data.mapper

import cn.gdeiassistant.R
import cn.gdeiassistant.data.text.DisplayTextResolver
import cn.gdeiassistant.model.DatingMyPost
import cn.gdeiassistant.model.DatingProfileCard
import cn.gdeiassistant.model.DatingProfileDetail
import cn.gdeiassistant.model.DatingReceivedPick
import cn.gdeiassistant.model.DatingSentPick
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies display-layer formatting to raw dating domain objects returned by
 * DatingRepository. Resolves localized fallback strings that were previously
 * baked into the repository layer.
 */
@Singleton
class DatingDisplayMapper @Inject constructor(
    private val textResolver: DisplayTextResolver
) {

    fun applyCardDefaults(card: DatingProfileCard): DatingProfileCard {
        return card.copy(
            nickname = card.nickname.ifBlank { textResolver.getString(R.string.dating_default_anonymous) },
            grade = gradeText(card.grade),
            faculty = card.faculty.ifBlank { textResolver.getString(R.string.dating_default_faculty_empty) },
            hometown = card.hometown.ifBlank { textResolver.getString(R.string.dating_default_hometown_empty) },
            content = card.content.ifBlank { textResolver.getString(R.string.dating_default_sent_message) }
        )
    }

    fun applyCardDefaults(cards: List<DatingProfileCard>): List<DatingProfileCard> {
        return cards.map(::applyCardDefaults)
    }

    fun applyDetailDefaults(detail: DatingProfileDetail): DatingProfileDetail {
        return detail.copy(
            profile = applyCardDefaults(detail.profile)
        )
    }

    fun applyReceivedPickDefaults(pick: DatingReceivedPick): DatingReceivedPick {
        return pick.copy(
            senderName = pick.senderName.ifBlank { textResolver.getString(R.string.dating_default_anonymous) },
            content = pick.content.ifBlank { textResolver.getString(R.string.dating_default_received_message) },
            time = pick.time.ifBlank { textResolver.getString(R.string.dating_default_recent_update) }
        )
    }

    fun applyReceivedPickDefaults(picks: List<DatingReceivedPick>): List<DatingReceivedPick> {
        return picks.map(::applyReceivedPickDefaults)
    }

    fun applySentPickDefaults(pick: DatingSentPick): DatingSentPick {
        return pick.copy(
            targetName = pick.targetName.ifBlank { textResolver.getString(R.string.dating_default_anonymous) },
            content = pick.content.ifBlank { textResolver.getString(R.string.dating_default_sent_message) }
        )
    }

    fun applySentPickDefaults(picks: List<DatingSentPick>): List<DatingSentPick> {
        return picks.map(::applySentPickDefaults)
    }

    fun applyMyPostDefaults(post: DatingMyPost): DatingMyPost {
        return post.copy(
            name = post.name.ifBlank { textResolver.getString(R.string.dating_default_anonymous) },
            publishTime = post.publishTime.ifBlank { textResolver.getString(R.string.dating_default_posted) },
            grade = gradeText(post.grade),
            faculty = post.faculty.ifBlank { textResolver.getString(R.string.dating_default_faculty_empty) },
            hometown = post.hometown.ifBlank { textResolver.getString(R.string.dating_default_hometown_empty) }
        )
    }

    fun applyMyPostDefaults(posts: List<DatingMyPost>): List<DatingMyPost> {
        return posts.map(::applyMyPostDefaults)
    }

    private fun gradeText(rawValue: String): String {
        return when (rawValue) {
            "1" -> textResolver.getString(R.string.dating_grade_one)
            "2" -> textResolver.getString(R.string.dating_grade_two)
            "3" -> textResolver.getString(R.string.dating_grade_three)
            "4" -> textResolver.getString(R.string.dating_grade_four)
            else -> textResolver.getString(R.string.dating_grade_unknown)
        }
    }
}
