package com.example.data.model

import com.example.R

data class CommunityComment(
    val id: String,
    val authorName: String,
    val authorAvatarEmoji: String,
    val authorBadge: String,
    val countryEmoji: String,
    val timestamp: String,
    val content: String,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false
)

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorAvatarEmoji: String,
    val authorBadge: String,
    val countryEmoji: String,
    val timestamp: String,
    val category: String, // "Riddles", "Math", "Physics", "Chemistry", "Biology", "History", "General"
    val difficulty: String, // "Normal", "Hard", "Grandmaster"
    val title: String,
    val content: String,
    val hint: String? = null,
    val solution: String? = null,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val comments: List<CommunityComment> = emptyList(),
    val isSolvedByMe: Boolean = false
)

data class CommunityPollOption(
    val id: String,
    val text: String,
    val votes: Int
)

data class CommunityPoll(
    val id: String,
    val question: String,
    val totalVotes: Int,
    val options: List<CommunityPollOption>,
    val userVotedOptionId: String? = null
)

data class CommunityCoopGoal(
    val title: String = "Worldwide Brainpower Sprint",
    val description: String = "Solve 10,000 puzzles collectively across all global thinkers this week to unlock the Mystic Science Chest!",
    val currentCount: Int = 7420,
    val targetCount: Int = 10000,
    val contributorsCount: Int = 1430,
    val rewardCoins: Int = 500,
    val rewardXp: Int = 1200,
    val isClaimed: Boolean = false
)

object CommunityCatalog {

    fun getInitialPoll(): CommunityPoll {
        return CommunityPoll(
            id = "poll_paradox_1",
            question = "Which scientific paradox breaks your intuition the most?",
            totalVotes = 3420,
            options = listOf(
                CommunityPollOption("opt_1", "🐱 Schrödinger's Cat (Quantum Superposition)", 1240),
                CommunityPollOption("opt_2", "🌌 Fermi Paradox (Where are all the aliens?)", 980),
                CommunityPollOption("opt_3", "⏳ Grandfather Paradox (Time causality loop)", 760),
                CommunityPollOption("opt_4", "🍩 Banach-Tarski Paradox (Geometric infinity)", 440)
            ),
            userVotedOptionId = null
        )
    }

    fun getInitialPosts(): List<CommunityPost> {
        return listOf(
            CommunityPost(
                id = "post_1",
                authorName = "Dr. Elena Rostova",
                authorAvatarEmoji = "⚡",
                authorBadge = "Quantum Savant",
                countryEmoji = "🇩🇪",
                timestamp = "2 hours ago",
                category = "Physics",
                difficulty = "Grandmaster",
                title = "The Light Beam & Mirror Paradox 🔦🪞",
                content = "If a spaceship travels at 99.9% the speed of light and you turn on a flashlight inside facing forward, how fast does the beam travel relative to an outside observer? And what does the astronaut see inside?",
                hint = "Think about Einstein's fundamental postulate of Special Relativity regarding constant c!",
                solution = "In Special Relativity, light speed 'c' (approx 300,000 km/s) is invariant in all inertial reference frames! Both the outside observer and the astronaut measure light traveling at exactly speed 'c', while time and length contract proportionally.",
                likesCount = 142,
                isLikedByMe = false,
                comments = listOf(
                    CommunityComment(
                        id = "c_1_1",
                        authorName = "Kenji Takahashi",
                        authorAvatarEmoji = "🧠",
                        authorBadge = "Grandmaster Logic",
                        countryEmoji = "🇯🇵",
                        timestamp = "1 hour ago",
                        content = "Lorentz transformation guarantees that speed of light in vacuum is always c. Time dilation compensates for the speed difference!",
                        likesCount = 38
                    ),
                    CommunityComment(
                        id = "c_1_2",
                        authorName = "Sophia Martinez",
                        authorAvatarEmoji = "🌸",
                        authorBadge = "Matrix Explorer",
                        countryEmoji = "🇲🇽",
                        timestamp = "35 mins ago",
                        content = "Mind completely blown every time I revisit special relativity. Great question!",
                        likesCount = 19
                    )
                )
            ),
            CommunityPost(
                id = "post_2",
                authorName = "Aarav Sharma",
                authorAvatarEmoji = "🚀",
                authorBadge = "STEM Prodigy",
                countryEmoji = "🇮🇳",
                timestamp = "4 hours ago",
                category = "Math",
                difficulty = "Hard",
                title = "The 3 Switches and 1 Lightbulb in the Attic 💡",
                content = "You are in the basement with 3 on/off switches. Only ONE switch controls the lightbulb in the closed attic upstairs. You can only make ONE trip upstairs to check. How do you find out with 100% certainty which switch controls the bulb?",
                hint = "Light bulbs produce more than just visible light when they are turned on for a while.",
                solution = "Turn ON Switch 1 and wait for 10 minutes (it heats up). Then turn OFF Switch 1 and turn ON Switch 2. Walk upstairs immediately: if the light is on -> Switch 2. If it is off but warm to touch -> Switch 1. If it is off and cold -> Switch 3!",
                likesCount = 289,
                isLikedByMe = true,
                comments = listOf(
                    CommunityComment(
                        id = "c_2_1",
                        authorName = "Lucas Dubois",
                        authorAvatarEmoji = "🎨",
                        authorBadge = "Speed Solver",
                        countryEmoji = "🇫🇷",
                        timestamp = "3 hours ago",
                        content = "Classic lateral thinking riddle! Utilizing thermal energy as the second bit of information.",
                        likesCount = 45
                    )
                )
            ),
            CommunityPost(
                id = "post_3",
                authorName = "Dr. Yuna Kim",
                authorAvatarEmoji = "💎",
                authorBadge = "Quantum Architect",
                countryEmoji = "🇰🇷",
                timestamp = "6 hours ago",
                category = "Chemistry",
                difficulty = "Normal",
                title = "Why does hot water freeze faster than cold water? (Mpemba Effect) ❄️",
                content = "We all learned thermodynamics, but Aristotle observed 2300 years ago that warm water can freeze faster than cold water under specific conditions. Recent 2020s microscopic studies point to hydrogen bond relaxation. What is your favorite explanation?",
                hint = "Hydrogen bond spacing, convective currents, and dissolved gas degassing all play subtle roles.",
                solution = "The Mpemba effect is primarily attributed to non-equilibrium thermal distributions, rapid convective currents in warm water, and hydrogen bond distance shrinkage as covalent O-H bonds store energy differently.",
                likesCount = 98,
                isLikedByMe = false,
                comments = listOf(
                    CommunityComment(
                        id = "c_3_1",
                        authorName = "Liam O'Connor",
                        authorAvatarEmoji = "🍀",
                        authorBadge = "Math Whiz",
                        countryEmoji = "🇮🇪",
                        timestamp = "5 hours ago",
                        content = "I tested this in our school lab with thermocouples! Super fascinating.",
                        likesCount = 12
                    )
                )
            ),
            CommunityPost(
                id = "post_4",
                authorName = "Julian Thorne",
                authorAvatarEmoji = "🥇",
                authorBadge = "Hall of Fame Titan",
                countryEmoji = "🇦🇺",
                timestamp = "8 hours ago",
                category = "Riddles",
                difficulty = "Normal",
                title = "The 8 Balls & Balance Scale Problem ⚖️",
                content = "You have 8 identical-looking metal spheres. 7 of them weigh exactly the same, but 1 is slightly heavier. Using a balance scale only TWICE, how can you identify the heavier ball?",
                hint = "Divide the 8 balls into groups of 3, 3, and 2.",
                solution = "Weigh 3 balls vs 3 balls. If they balance, weigh the remaining 2 balls against each other to find the heavy one. If the scale tips, pick the heavier group of 3, and weigh 1 vs 1 (if equal, the 3rd unweighed ball is heavy!).",
                likesCount = 310,
                isLikedByMe = false,
                comments = listOf(
                    CommunityComment(
                        id = "c_4_1",
                        authorName = "Emma Watson-Lee",
                        authorAvatarEmoji = "🔬",
                        authorBadge = "Lab Researcher",
                        countryEmoji = "🇬🇧",
                        timestamp = "7 hours ago",
                        content = "Information theory at its best: 3 outcomes per weighing (left heavy, right heavy, equal) gives 3^2 = 9 possible states, which is enough for 8 balls!",
                        likesCount = 54
                    )
                )
            ),
            CommunityPost(
                id = "post_5",
                authorName = "Prof. Alexander Vance",
                authorAvatarEmoji = "👑",
                authorBadge = "Apex Grandmaster",
                countryEmoji = "🇨🇦",
                timestamp = "12 hours ago",
                category = "History",
                difficulty = "Hard",
                title = "The Antikythera Mechanism: Ancient Greek Analog Computer ⚙️",
                content = "Discovered in 1901 from a shipwreck off Greece, this 2,000-year-old bronze device contained over 30 precision gears that tracked planetary orbits and predicted solar eclipses centuries ahead of its time. What amazed historians the most about its differential gear train?",
                hint = "It simulated the irregular elliptical motion of the Moon (Kepler's laws before Kepler!).",
                solution = "The gear train included a pin-and-slot mechanism that modeled the Moon's variable speed caused by its eccentric orbit (Hipparchus's lunar theory), representing technology that was not seen again until 14th-century astronomical clocks in Europe.",
                likesCount = 205,
                isLikedByMe = false,
                comments = emptyList()
            )
        )
    }
}
