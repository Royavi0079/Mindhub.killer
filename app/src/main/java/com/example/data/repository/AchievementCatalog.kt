package com.example.data.repository

import com.example.data.model.AchievementBadge
import com.example.data.model.AchievementCategory
import com.example.data.model.BadgeTier
import com.example.data.model.PuzzleCategory
import com.example.data.model.UserProgress

object AchievementCatalog {

    val allBadges: List<AchievementBadge> = listOf(
        // =========================================================================
        // 1. LEVEL PROGRESSION & PUZZLE MILESTONES (15 Achievements)
        // =========================================================================
        AchievementBadge(
            id = "badge_first_puzzle",
            title = "First Step",
            description = "Solve your very first puzzle quest in MindMatrix",
            iconEmoji = "🌱",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 100,
            rewardCoins = 50,
            loreDescription = "Every genius began with a single curiosity. You have taken your first bold step into the matrix of human knowledge."
        ),
        AchievementBadge(
            id = "badge_lvl_1_initiate",
            title = "Level 1 Explorer",
            description = "Conquer Level 1 in any science, math or visual category",
            iconEmoji = "🥉",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 80,
            rewardCoins = 40,
            loreDescription = "Level 1 mastered! You have proven fundamental reasoning and spatial comprehension."
        ),
        AchievementBadge(
            id = "badge_lvl_2_scholar",
            title = "Level 2 Scholar",
            description = "Conquer Level 2 in any subject category",
            iconEmoji = "🥈",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 2,
            rewardXp = 120,
            rewardCoins = 60,
            loreDescription = "Stepping up the difficulty! Level 2 unlocks deeper formulas and multi-step logic."
        ),
        AchievementBadge(
            id = "badge_lvl_3_deducer",
            title = "Level 3 Deducer",
            description = "Conquer Level 3 in any subject category",
            iconEmoji = "🥇",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 3,
            rewardXp = 180,
            rewardCoins = 90,
            loreDescription = "Level 3 conquered! Synaptic stamina is expanding as deductive reasoning sharpens."
        ),
        AchievementBadge(
            id = "badge_lvl_4_analyst",
            title = "Level 4 Analyst",
            description = "Conquer Level 4 in any subject category",
            iconEmoji = "🔬",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 4,
            rewardXp = 250,
            rewardCoins = 120,
            loreDescription = "Analytical prowess at work! Complex variables and intricate tile positions yield to your focus."
        ),
        AchievementBadge(
            id = "badge_lvl_5_virtuoso",
            title = "Level 5 Virtuoso",
            description = "Conquer Level 5 in any subject category",
            iconEmoji = "💠",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 5,
            rewardXp = 350,
            rewardCoins = 175,
            loreDescription = "Halfway through the core curriculum! Level 5 puzzles require profound pattern synthesis."
        ),
        AchievementBadge(
            id = "badge_lvl_6_master",
            title = "Level 6 Master",
            description = "Conquer Level 6 in any subject category",
            iconEmoji = "💎",
            tier = BadgeTier.PLATINUM,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 6,
            rewardXp = 500,
            rewardCoins = 250,
            loreDescription = "Level 6 mastery places your intellect in the top tier of cognitive thinkers."
        ),
        AchievementBadge(
            id = "badge_lvl_7_grandmaster",
            title = "Level 7 Grandmaster",
            description = "Conquer Level 7 in any subject category",
            iconEmoji = "👑",
            tier = BadgeTier.DIAMOND,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 7,
            rewardXp = 750,
            rewardCoins = 350,
            loreDescription = "The pinnacle of the primary syllabus! Level 7 demands absolute mastery of quantum, chemical and mathematical principles."
        ),
        AchievementBadge(
            id = "badge_lvl_8_sage",
            title = "Level 8 Sage",
            description = "Conquer Level 8 in procedural advanced challenges",
            iconEmoji = "🌌",
            tier = BadgeTier.DIAMOND,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 8,
            rewardXp = 900,
            rewardCoins = 450,
            loreDescription = "Venturing beyond normal bounds into the endless cosmos of procedural puzzles."
        ),
        AchievementBadge(
            id = "badge_lvl_9_luminary",
            title = "Level 9 Luminary",
            description = "Conquer Level 9 in procedural advanced challenges",
            iconEmoji = "✨",
            tier = BadgeTier.MYTHIC,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 9,
            rewardXp = 1200,
            rewardCoins = 600,
            loreDescription = "Your deductive agility shines like a beacon across the global leaderboard."
        ),
        AchievementBadge(
            id = "badge_lvl_10_titan",
            title = "Level 10 Titan of Logic",
            description = "Conquer Level 10 and unlock elite prestige status",
            iconEmoji = "🏆",
            tier = BadgeTier.MYTHIC,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 10,
            rewardXp = 1500,
            rewardCoins = 750,
            loreDescription = "A true Titan! Level 10 solved proves unmatched mastery across all STEM puzzle matrices."
        ),
        AchievementBadge(
            id = "badge_novice_5",
            title = "Puzzle Novice",
            description = "Solve 5 puzzles across any science or logic category",
            iconEmoji = "🧩",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 5,
            rewardXp = 200,
            rewardCoins = 100,
            loreDescription = "Pattern recognition and spatial awareness are taking root in your reasoning faculties."
        ),
        AchievementBadge(
            id = "badge_builder_10",
            title = "Brain Builder",
            description = "Solve 10 puzzles and build your synaptic stamina",
            iconEmoji = "🧠",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 10,
            rewardXp = 350,
            rewardCoins = 175,
            loreDescription = "A consistent intellect strengthens memory pathways and accelerates cognitive deduction."
        ),
        AchievementBadge(
            id = "badge_adept_25",
            title = "Puzzle Adept",
            description = "Conquer 25 challenging logic and STEM puzzles",
            iconEmoji = "🎯",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 25,
            rewardXp = 750,
            rewardCoins = 350,
            loreDescription = "Quarter of a century solved! You have proven remarkable tenacity across multiple scientific disciplines."
        ),
        AchievementBadge(
            id = "badge_master_50",
            title = "Half-Century Mind Master",
            description = "Solve 50 puzzles across the MindMatrix universe",
            iconEmoji = "💎",
            tier = BadgeTier.DIAMOND,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 50,
            rewardXp = 1500,
            rewardCoins = 750,
            loreDescription = "A monumental milestone! 50 puzzles solved places you in the upper echelon of global thinkers."
        ),
        AchievementBadge(
            id = "badge_centurion_100",
            title = "Centurion Grandmaster",
            description = "Solve 100 puzzles to achieve legendary mind status",
            iconEmoji = "👑",
            tier = BadgeTier.MYTHIC,
            category = AchievementCategory.MILESTONES,
            currentProgress = 0,
            targetProgress = 100,
            rewardXp = 3000,
            rewardCoins = 1500,
            loreDescription = "A true century of brilliance! Your mind is a fortress of rapid logic, boundless curiosity, and unwavering deduction."
        ),

        // =========================================================================
        // 2. CATEGORY MASTERY ACHIEVEMENTS (15 Achievements)
        // =========================================================================
        AchievementBadge(
            id = "badge_visual_tier1",
            title = "Jigsaw Initiate",
            description = "Solve 3 levels of Visual & Jigsaw sliding puzzles",
            iconEmoji = "🧩",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.VISUAL,
            currentProgress = 0,
            targetProgress = 3,
            rewardXp = 200,
            rewardCoins = 100,
            loreDescription = "Reassembling fractured visual matrices with spatial precision."
        ),
        AchievementBadge(
            id = "badge_visual_master",
            title = "Visual Virtuoso",
            description = "Complete all 7 levels of Visual & Jigsaw sliding puzzles",
            iconEmoji = "🖼️",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.VISUAL,
            currentProgress = 0,
            targetProgress = 7,
            rewardXp = 500,
            rewardCoins = 250,
            loreDescription = "Mastery of visual fragmentation and rapid reconstruction. Your spatial intuition is razor-sharp."
        ),
        AchievementBadge(
            id = "badge_math_tier1",
            title = "Math Apprentice",
            description = "Solve 3 levels of Mathematics & Equation puzzles",
            iconEmoji = "🔢",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.MATH,
            currentProgress = 0,
            targetProgress = 3,
            rewardXp = 200,
            rewardCoins = 100,
            loreDescription = "Mastering arithmetic operations and algebraic fundamentals."
        ),
        AchievementBadge(
            id = "badge_math_master",
            title = "Math Mathemagician",
            description = "Complete all 7 levels of Mathematics & Equations",
            iconEmoji = "➗",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.MATH,
            currentProgress = 0,
            targetProgress = 7,
            rewardXp = 500,
            rewardCoins = 250,
            loreDescription = "From prime sequences to Euler's identities, numerical patterns hold no secrets from you."
        ),
        AchievementBadge(
            id = "badge_physics_tier1",
            title = "Mechanics Initiate",
            description = "Solve 3 levels of Physics & Optics puzzles",
            iconEmoji = "⚙️",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.PHYSICS,
            currentProgress = 0,
            targetProgress = 3,
            rewardXp = 200,
            rewardCoins = 100,
            loreDescription = "Grasping velocity, kinetic energy, and circuit logic."
        ),
        AchievementBadge(
            id = "badge_physics_master",
            title = "Physics Phenomenon",
            description = "Complete all 7 levels of Physics & Mechanics",
            iconEmoji = "⚡",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.PHYSICS,
            currentProgress = 0,
            targetProgress = 7,
            rewardXp = 500,
            rewardCoins = 250,
            loreDescription = "Master of quantum forces, relativistic speed, and thermodynamic equilibrium."
        ),
        AchievementBadge(
            id = "badge_chemistry_tier1",
            title = "Molecular Explorer",
            description = "Solve 3 levels of Chemistry & Reactions puzzles",
            iconEmoji = "⚗️",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.CHEMISTRY,
            currentProgress = 0,
            targetProgress = 3,
            rewardXp = 200,
            rewardCoins = 100,
            loreDescription = "Exploring ionic bonds, atomic valence, and catalytic reactions."
        ),
        AchievementBadge(
            id = "badge_chemistry_master",
            title = "Alchemist Supreme",
            description = "Complete all 7 levels of Chemistry & Reactions",
            iconEmoji = "🧪",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.CHEMISTRY,
            currentProgress = 0,
            targetProgress = 7,
            rewardXp = 500,
            rewardCoins = 250,
            loreDescription = "From noble electron clouds to exothermic wonders, you synthesize elements with supreme precision."
        ),
        AchievementBadge(
            id = "badge_biology_tier1",
            title = "Cellular Biologist",
            description = "Solve 3 levels of Biology & Anatomy puzzles",
            iconEmoji = "🌿",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.BIOLOGY,
            currentProgress = 0,
            targetProgress = 3,
            rewardXp = 200,
            rewardCoins = 100,
            loreDescription = "Understanding mitochondria, cellular respiration, and DNA coding."
        ),
        AchievementBadge(
            id = "badge_biology_master",
            title = "Bio-Genetics Pioneer",
            description = "Complete all 7 levels of Biology & Cellular Life",
            iconEmoji = "🧬",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.BIOLOGY,
            currentProgress = 0,
            targetProgress = 7,
            rewardXp = 500,
            rewardCoins = 250,
            loreDescription = "Unraveler of the double-helix, cellular metabolism, and ecological balance across Earth."
        ),
        AchievementBadge(
            id = "badge_history_tier1",
            title = "Time Voyager",
            description = "Solve 3 levels of Ancient History & Civilizations",
            iconEmoji = "📜",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.HISTORY,
            currentProgress = 0,
            targetProgress = 3,
            rewardXp = 200,
            rewardCoins = 100,
            loreDescription = "Exploring the cradle of civilization and early empires."
        ),
        AchievementBadge(
            id = "badge_history_master",
            title = "Chronicler of Time",
            description = "Complete all 7 levels of Ancient History & Civilizations",
            iconEmoji = "🏛️",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.CATEGORY_MASTERY,
            targetCategory = PuzzleCategory.HISTORY,
            currentProgress = 0,
            targetProgress = 7,
            rewardXp = 500,
            rewardCoins = 250,
            loreDescription = "Bearer of the historical tapestry. From ancient Mesopotamia to the Renaissance, you hold the timeline of humanity."
        ),
        AchievementBadge(
            id = "badge_grandmaster_polymath",
            title = "Grandmaster Polymath",
            description = "Master all 6 primary subject categories (42/42 levels complete)",
            iconEmoji = "👑",
            tier = BadgeTier.MYTHIC,
            category = AchievementCategory.CATEGORY_MASTERY,
            currentProgress = 0,
            targetProgress = 42,
            rewardXp = 2500,
            rewardCoins = 1200,
            loreDescription = "The supreme Renaissance Mind. You have achieved total mastery across all STEM and human history disciplines."
        ),

        // =========================================================================
        // 3. REWARDED VIDEO & AD PATRON ACHIEVEMENTS (10+ Achievements: 20+ Coins Per Watch)
        // =========================================================================
        AchievementBadge(
            id = "badge_video_patron_1",
            title = "First Video Patron",
            description = "Watch 1 rewarded video ad to earn bonus coins & powerups",
            iconEmoji = "📺",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 100,
            rewardCoins = 50,
            loreDescription = "Supporting the app while stacking your coin treasury with lucrative rewards!"
        ),
        AchievementBadge(
            id = "badge_video_patron_3",
            title = "Triple Video Ally",
            description = "Watch 3 rewarded videos in the Task Center or celebrations",
            iconEmoji = "🎬",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 3,
            rewardXp = 200,
            rewardCoins = 100,
            loreDescription = "Triple video booster! You claimed 150+ extra coins from the daily cycle."
        ),
        AchievementBadge(
            id = "badge_video_patron_5",
            title = "Commercial Connoisseur",
            description = "Watch 5 rewarded videos for bonus coins and double rewards",
            iconEmoji = "💰",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 5,
            rewardXp = 350,
            rewardCoins = 175,
            loreDescription = "A true patron of science puzzles! Your coin vault is flourishing."
        ),
        AchievementBadge(
            id = "badge_video_patron_10",
            title = "Ad Benefactor Supreme",
            description = "Watch 10 rewarded videos across puzzle victories & refill stations",
            iconEmoji = "💎",
            tier = BadgeTier.PLATINUM,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 10,
            rewardXp = 600,
            rewardCoins = 300,
            loreDescription = "10 rewarded video boosts claimed! Maximize double victory payouts at will."
        ),
        AchievementBadge(
            id = "badge_video_patron_20",
            title = "Grand Patron of Science",
            description = "Watch 20 rewarded videos to attain elite supporter status",
            iconEmoji = "👑",
            tier = BadgeTier.MYTHIC,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 20,
            rewardXp = 1200,
            rewardCoins = 600,
            loreDescription = "The ultimate benefactor! Hundreds of extra coins collected, propelling you to top rank."
        ),
        AchievementBadge(
            id = "badge_video_level_bonus",
            title = "Level Bounty Hunter",
            description = "Watch a Level Completion Bonus Video for +100 Extra Coins",
            iconEmoji = "🪙",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 150,
            rewardCoins = 75,
            loreDescription = "Claimed the extra +100 coin victory video jackpot!"
        ),
        AchievementBadge(
            id = "badge_video_2x_claimed",
            title = "Double Victory Alchemist",
            description = "Claim a 2X Double XP & Coins video reward on victory",
            iconEmoji = "⚡",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 150,
            rewardCoins = 75,
            loreDescription = "Doubling your triumph rewards with a swift video boost."
        ),
        AchievementBadge(
            id = "badge_video_hint_refill",
            title = "Infinite Intellect",
            description = "Refill +4 Free Hints by watching an Interstitial / Rewarded ad",
            iconEmoji = "💡",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 100,
            rewardCoins = 50,
            loreDescription = "Unlimited wisdom! Replenished hint supplies to conquer tricky dilemmas."
        ),

        // =========================================================================
        // 4. SKILL & SPEED ACHIEVEMENTS (10 Achievements)
        // =========================================================================
        AchievementBadge(
            id = "badge_speed_demon",
            title = "Speed Demon",
            description = "Solve any puzzle with swift precision under 45 seconds",
            iconEmoji = "⏱️",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.SKILL_SPEED,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 300,
            rewardCoins = 150,
            loreDescription = "Lightning-quick reflex paired with pinpoint deductive clarity under pressure."
        ),
        AchievementBadge(
            id = "badge_lightning_solver",
            title = "Lightning Solver",
            description = "Solve any STEM problem in under 15 seconds",
            iconEmoji = "⚡",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.SKILL_SPEED,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 400,
            rewardCoins = 200,
            loreDescription = "Instinctive genius! The correct deduction was instantaneous."
        ),
        AchievementBadge(
            id = "badge_star_collector",
            title = "Stellar Prodigy",
            description = "Accumulate 15 total Gold Stars from high-scoring puzzles",
            iconEmoji = "⭐",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.SKILL_SPEED,
            currentProgress = 0,
            targetProgress = 15,
            rewardXp = 300,
            rewardCoins = 150,
            loreDescription = "15 stars earned through flawless deductions and minimal moves."
        ),
        AchievementBadge(
            id = "badge_star_master_30",
            title = "Constellation Master",
            description = "Accumulate 30 total Gold Stars across puzzles",
            iconEmoji = "🌟",
            tier = BadgeTier.PLATINUM,
            category = AchievementCategory.SKILL_SPEED,
            currentProgress = 0,
            targetProgress = 30,
            rewardXp = 600,
            rewardCoins = 300,
            loreDescription = "A glowing constellation of 30 stars awarded for premier performance."
        ),
        AchievementBadge(
            id = "badge_star_titan_50",
            title = "Galactic Nova",
            description = "Accumulate 50 total Gold Stars from expert completions",
            iconEmoji = "💫",
            tier = BadgeTier.DIAMOND,
            category = AchievementCategory.SKILL_SPEED,
            currentProgress = 0,
            targetProgress = 50,
            rewardXp = 1000,
            rewardCoins = 500,
            loreDescription = "50 stars shining in your galaxy of mind mastery!"
        ),
        AchievementBadge(
            id = "badge_extreme_conqueror",
            title = "Grandmaster Mindbender",
            description = "Conquer a Tier III Grandmaster Extreme Mindbender puzzle",
            iconEmoji = "🔥",
            tier = BadgeTier.DIAMOND,
            category = AchievementCategory.SKILL_SPEED,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 800,
            rewardCoins = 400,
            loreDescription = "Extreme tier puzzles baffle ordinary minds. You cracked the code without flinching."
        ),
        AchievementBadge(
            id = "badge_sliding_minimalist",
            title = "Minimalist Move Master",
            description = "Solve a sliding puzzle in minimal moves",
            iconEmoji = "🎯",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.SKILL_SPEED,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 450,
            rewardCoins = 225,
            loreDescription = "Optimal path computation with zero redundant moves."
        ),

        // =========================================================================
        // 5. SPECIAL FEATS & TREASURY (10 Achievements)
        // =========================================================================
        AchievementBadge(
            id = "badge_ai_pioneer",
            title = "AI Lab Pioneer",
            description = "Generate and conquer 3 custom STEM challenges in AI Lab",
            iconEmoji = "🤖",
            tier = BadgeTier.PLATINUM,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 3,
            rewardXp = 450,
            rewardCoins = 200,
            loreDescription = "Harnessing the Gemini neural engine to construct and solve unique scientific quandaries."
        ),
        AchievementBadge(
            id = "badge_treasure_vault_100",
            title = "Coin Collector",
            description = "Accumulate 100 Coins in your treasury",
            iconEmoji = "🪙",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 100,
            rewardXp = 150,
            rewardCoins = 50,
            loreDescription = "Your initial coin hoard is growing through clever puzzle solving."
        ),
        AchievementBadge(
            id = "badge_treasure_vault",
            title = "Treasure Vault",
            description = "Accumulate over 500 Coins in your personal treasury",
            iconEmoji = "💰",
            tier = BadgeTier.PLATINUM,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 500,
            rewardXp = 500,
            rewardCoins = 250,
            loreDescription = "A wealthy intellectual! Your coin hoard reflects relentless problem solving and bounty collection."
        ),
        AchievementBadge(
            id = "badge_treasure_millionaire",
            title = "Matrix Millionaire",
            description = "Accumulate 1,000 Coins in your personal treasury",
            iconEmoji = "🏦",
            tier = BadgeTier.MYTHIC,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 1000,
            rewardXp = 1200,
            rewardCoins = 500,
            loreDescription = "A 4-digit treasury! You possess supreme puzzle wealth and prestige."
        ),
        AchievementBadge(
            id = "badge_daily_streak_3",
            title = "Dedication Champion",
            description = "Maintain a 3-day active streak in MindMatrix",
            iconEmoji = "🔥",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 3,
            rewardXp = 300,
            rewardCoins = 150,
            loreDescription = "Daily discipline turns knowledge into intuition and potential into undeniable mastery."
        ),
        AchievementBadge(
            id = "badge_daily_streak_7",
            title = "Weekly Legend",
            description = "Maintain a 7-day continuous learning streak",
            iconEmoji = "🌟",
            tier = BadgeTier.DIAMOND,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 7,
            rewardXp = 800,
            rewardCoins = 400,
            loreDescription = "A full unbroken week of daily intellect elevation!"
        ),
        AchievementBadge(
            id = "badge_avatar_stylist",
            title = "Avatar Stylist",
            description = "Customize your profile border or theme in Stats Hall",
            iconEmoji = "🎨",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 100,
            rewardCoins = 50,
            loreDescription = "Personalized identity reflects the unique style of your brilliant mind."
        ),
        AchievementBadge(
            id = "badge_task_hunter",
            title = "Daily Bounty Hunter",
            description = "Claim 5 daily task or milestone coin rewards",
            iconEmoji = "🎯",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 5,
            rewardXp = 250,
            rewardCoins = 125,
            loreDescription = "Diligently completing daily missions and securing maximum bounties."
        ),
        AchievementBadge(
            id = "badge_community_champion",
            title = "Community Contributor",
            description = "Engage with the Community Hub and peer thinkers",
            iconEmoji = "🌍",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            targetProgress = 1,
            rewardXp = 150,
            rewardCoins = 75,
            loreDescription = "Collaborating with fellow thinkers to elevate human understanding."
        )
    )

    /**
     * Computes real-time progress for all achievement badges based on the user's current progress state.
     */
    fun evaluateBadges(userProgress: UserProgress): List<AchievementBadge> {
        val unlockedSet = userProgress.unlockedBadgeIds
        val maxSolvedLevelAcrossAll = userProgress.categorySolvedCounts.values.maxOrNull() ?: 0

        return allBadges.map { badge ->
            val isExplicitlyUnlocked = unlockedSet.contains(badge.id)

            val currentProgress = when (badge.id) {
                // Milestones & Level progressions
                "badge_first_puzzle" -> userProgress.totalPuzzlesSolved.coerceAtMost(badge.targetProgress)
                "badge_lvl_1_initiate" -> if (userProgress.totalPuzzlesSolved >= 1) 1 else 0
                "badge_lvl_2_scholar" -> if (maxSolvedLevelAcrossAll >= 2 || userProgress.totalPuzzlesSolved >= 2) 2 else 0
                "badge_lvl_3_deducer" -> if (maxSolvedLevelAcrossAll >= 3 || userProgress.totalPuzzlesSolved >= 3) 3 else 0
                "badge_lvl_4_analyst" -> if (maxSolvedLevelAcrossAll >= 4 || userProgress.totalPuzzlesSolved >= 4) 4 else 0
                "badge_lvl_5_virtuoso" -> if (maxSolvedLevelAcrossAll >= 5 || userProgress.totalPuzzlesSolved >= 5) 5 else 0
                "badge_lvl_6_master" -> if (maxSolvedLevelAcrossAll >= 6 || userProgress.totalPuzzlesSolved >= 6) 6 else 0
                "badge_lvl_7_grandmaster" -> if (maxSolvedLevelAcrossAll >= 7 || userProgress.totalPuzzlesSolved >= 7) 7 else 0
                "badge_lvl_8_sage" -> if (maxSolvedLevelAcrossAll >= 8 || userProgress.totalPuzzlesSolved >= 8) 8 else 0
                "badge_lvl_9_luminary" -> if (maxSolvedLevelAcrossAll >= 9 || userProgress.totalPuzzlesSolved >= 9) 9 else 0
                "badge_lvl_10_titan" -> if (maxSolvedLevelAcrossAll >= 10 || userProgress.totalPuzzlesSolved >= 10) 10 else 0

                "badge_novice_5" -> userProgress.totalPuzzlesSolved.coerceAtMost(badge.targetProgress)
                "badge_builder_10" -> userProgress.totalPuzzlesSolved.coerceAtMost(badge.targetProgress)
                "badge_adept_25" -> userProgress.totalPuzzlesSolved.coerceAtMost(badge.targetProgress)
                "badge_master_50" -> userProgress.totalPuzzlesSolved.coerceAtMost(badge.targetProgress)
                "badge_centurion_100" -> userProgress.totalPuzzlesSolved.coerceAtMost(badge.targetProgress)

                // Category Mastery
                "badge_visual_tier1" -> (userProgress.categorySolvedCounts[PuzzleCategory.VISUAL.id] ?: 0).coerceAtMost(3)
                "badge_visual_master" -> (userProgress.categorySolvedCounts[PuzzleCategory.VISUAL.id] ?: 0).coerceAtMost(7)
                "badge_math_tier1" -> (userProgress.categorySolvedCounts[PuzzleCategory.MATH.id] ?: 0).coerceAtMost(3)
                "badge_math_master" -> (userProgress.categorySolvedCounts[PuzzleCategory.MATH.id] ?: 0).coerceAtMost(7)
                "badge_physics_tier1" -> (userProgress.categorySolvedCounts[PuzzleCategory.PHYSICS.id] ?: 0).coerceAtMost(3)
                "badge_physics_master" -> (userProgress.categorySolvedCounts[PuzzleCategory.PHYSICS.id] ?: 0).coerceAtMost(7)
                "badge_chemistry_tier1" -> (userProgress.categorySolvedCounts[PuzzleCategory.CHEMISTRY.id] ?: 0).coerceAtMost(3)
                "badge_chemistry_master" -> (userProgress.categorySolvedCounts[PuzzleCategory.CHEMISTRY.id] ?: 0).coerceAtMost(7)
                "badge_biology_tier1" -> (userProgress.categorySolvedCounts[PuzzleCategory.BIOLOGY.id] ?: 0).coerceAtMost(3)
                "badge_biology_master" -> (userProgress.categorySolvedCounts[PuzzleCategory.BIOLOGY.id] ?: 0).coerceAtMost(7)
                "badge_history_tier1" -> (userProgress.categorySolvedCounts[PuzzleCategory.HISTORY.id] ?: 0).coerceAtMost(3)
                "badge_history_master" -> (userProgress.categorySolvedCounts[PuzzleCategory.HISTORY.id] ?: 0).coerceAtMost(7)
                "badge_grandmaster_polymath" -> {
                    val core6Solved = (userProgress.categorySolvedCounts[PuzzleCategory.VISUAL.id] ?: 0).coerceAtMost(7) +
                            (userProgress.categorySolvedCounts[PuzzleCategory.MATH.id] ?: 0).coerceAtMost(7) +
                            (userProgress.categorySolvedCounts[PuzzleCategory.PHYSICS.id] ?: 0).coerceAtMost(7) +
                            (userProgress.categorySolvedCounts[PuzzleCategory.CHEMISTRY.id] ?: 0).coerceAtMost(7) +
                            (userProgress.categorySolvedCounts[PuzzleCategory.BIOLOGY.id] ?: 0).coerceAtMost(7) +
                            (userProgress.categorySolvedCounts[PuzzleCategory.HISTORY.id] ?: 0).coerceAtMost(7)
                    core6Solved.coerceAtMost(42)
                }

                // Video Rewards & Ad Patron (20+ Coins and Achievement per watch)
                "badge_video_patron_1" -> userProgress.adsWatchedCount.coerceAtMost(badge.targetProgress)
                "badge_video_patron_3" -> userProgress.adsWatchedCount.coerceAtMost(badge.targetProgress)
                "badge_video_patron_5" -> userProgress.adsWatchedCount.coerceAtMost(badge.targetProgress)
                "badge_video_patron_10" -> userProgress.adsWatchedCount.coerceAtMost(badge.targetProgress)
                "badge_video_patron_20" -> userProgress.adsWatchedCount.coerceAtMost(badge.targetProgress)
                "badge_video_level_bonus" -> if (userProgress.adsWatchedCount >= 1 || isExplicitlyUnlocked) 1 else 0
                "badge_video_2x_claimed" -> if (userProgress.adsWatchedCount >= 1 || isExplicitlyUnlocked) 1 else 0
                "badge_video_hint_refill" -> if (userProgress.hintsUsedCount >= 4 || userProgress.adsWatchedCount >= 1 || isExplicitlyUnlocked) 1 else 0

                // Skill & Speed
                "badge_speed_demon" -> if (isExplicitlyUnlocked || userProgress.totalPuzzlesSolved >= 1) 1 else 0
                "badge_lightning_solver" -> if (isExplicitlyUnlocked || userProgress.totalPuzzlesSolved >= 2) 1 else 0
                "badge_star_collector" -> userProgress.starsEarned.coerceAtMost(badge.targetProgress)
                "badge_star_master_30" -> userProgress.starsEarned.coerceAtMost(badge.targetProgress)
                "badge_star_titan_50" -> userProgress.starsEarned.coerceAtMost(badge.targetProgress)
                "badge_extreme_conqueror" -> if (isExplicitlyUnlocked || userProgress.starsEarned >= 15) 1 else 0
                "badge_sliding_minimalist" -> if (isExplicitlyUnlocked || (userProgress.categorySolvedCounts[PuzzleCategory.VISUAL.id] ?: 0) >= 1) 1 else 0

                // Special Feats
                "badge_ai_pioneer" -> (userProgress.categorySolvedCounts[PuzzleCategory.AI_LAB.id] ?: 0).coerceAtMost(badge.targetProgress)
                "badge_treasure_vault_100" -> userProgress.coins.coerceAtMost(badge.targetProgress)
                "badge_treasure_vault" -> userProgress.coins.coerceAtMost(badge.targetProgress)
                "badge_treasure_millionaire" -> userProgress.coins.coerceAtMost(badge.targetProgress)
                "badge_daily_streak_3" -> userProgress.currentStreak.coerceAtMost(badge.targetProgress)
                "badge_daily_streak_7" -> userProgress.currentStreak.coerceAtMost(badge.targetProgress)
                "badge_avatar_stylist" -> if (userProgress.userProfileBackground != "COSMIC_NEBULA" || userProgress.userAvatarBorder != "NEON_CYAN" || isExplicitlyUnlocked) 1 else 0
                "badge_task_hunter" -> userProgress.claimedTaskIds.size.coerceAtMost(badge.targetProgress)
                "badge_community_champion" -> if (userProgress.communityContributedCount > 0 || isExplicitlyUnlocked) 1 else 0

                else -> if (isExplicitlyUnlocked) badge.targetProgress else 0
            }

            val isUnlocked = isExplicitlyUnlocked || (currentProgress >= badge.targetProgress)

            badge.copy(
                currentProgress = if (isUnlocked) badge.targetProgress else currentProgress,
                isUnlocked = isUnlocked
            )
        }
    }
}
