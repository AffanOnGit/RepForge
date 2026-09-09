package com.repforge.core.data.database.seed

import com.repforge.core.data.database.entity.ExerciseEntity

/**
 * Curated seed dictionary of ~250 exercises covering all 19 sub-muscles and 7 equipment types.
 * Each entry includes primary and secondary sub-muscles, equipment, and creator/scientific tags.
 */
object ExerciseSeedData {

    val exercises: List<ExerciseEntity> = listOf(
        // ==========================================
        // CHEST: UPPER CHEST (Clavicular Head)
        // ==========================================
        ExerciseEntity(
            id = "ex_incline_bb_bench",
            name = "Incline Barbell Bench Press",
            equipment = "BARBELL",
            primarySubMuscle = "UPPER_CHEST",
            secondarySubMuscles = "FRONT_DELTS,TRICEPS,MID_CHEST",
            creatorTags = "#JeffNippard,#Hypertrophy,#Compound"
        ),
        ExerciseEntity(
            id = "ex_incline_db_press",
            name = "Incline Dumbbell Press",
            equipment = "DUMBBELL",
            primarySubMuscle = "UPPER_CHEST",
            secondarySubMuscles = "FRONT_DELTS,TRICEPS,MID_CHEST",
            creatorTags = "#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_incline_smith_press",
            name = "Incline Smith Machine Press",
            equipment = "SMITH_MACHINE",
            primarySubMuscle = "UPPER_CHEST",
            secondarySubMuscles = "FRONT_DELTS,TRICEPS",
            creatorTags = "#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_incline_cable_flye",
            name = "Low-to-High Cable Flye",
            equipment = "CABLE",
            primarySubMuscle = "UPPER_CHEST",
            secondarySubMuscles = "FRONT_DELTS",
            creatorTags = "#JeffNippard,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_incline_db_flye",
            name = "Incline Dumbbell Flye",
            equipment = "DUMBBELL",
            primarySubMuscle = "UPPER_CHEST",
            secondarySubMuscles = "FRONT_DELTS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_incline_machine_press",
            name = "Incline Chest Press Machine",
            equipment = "MACHINE",
            primarySubMuscle = "UPPER_CHEST",
            secondarySubMuscles = "TRICEPS,FRONT_DELTS",
            creatorTags = "#Hypertrophy,#Machine"
        ),
        ExerciseEntity(
            id = "ex_decline_pushup",
            name = "Decline Push-Up (Feet Elevated)",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "UPPER_CHEST",
            secondarySubMuscles = "FRONT_DELTS,TRICEPS,UPPER_ABS",
            creatorTags = "#Calisthenics,#Bodyweight"
        ),
        ExerciseEntity(
            id = "ex_reverse_grip_bb_bench",
            name = "Reverse-Grip Barbell Bench Press",
            equipment = "BARBELL",
            primarySubMuscle = "UPPER_CHEST",
            secondarySubMuscles = "TRICEPS,MID_CHEST",
            creatorTags = "#AthleanX"
        ),
        ExerciseEntity(
            id = "ex_landmine_chest_press",
            name = "Standing Incline Landmine Press",
            equipment = "BARBELL",
            primarySubMuscle = "UPPER_CHEST",
            secondarySubMuscles = "FRONT_DELTS,TRICEPS",
            creatorTags = "#AthleanX,#Functional"
        ),
        ExerciseEntity(
            id = "ex_kb_incline_press",
            name = "Incline Kettlebell Press",
            equipment = "KETTLEBELL",
            primarySubMuscle = "UPPER_CHEST",
            secondarySubMuscles = "FRONT_DELTS,TRICEPS",
            creatorTags = "#Kettlebell"
        ),

        // ==========================================
        // CHEST: MID CHEST (Sternal Head)
        // ==========================================
        ExerciseEntity(
            id = "ex_flat_bb_bench",
            name = "Barbell Flat Bench Press",
            equipment = "BARBELL",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "FRONT_DELTS,TRICEPS,UPPER_CHEST",
            creatorTags = "#JeffNippard,#Compound,#Strength"
        ),
        ExerciseEntity(
            id = "ex_flat_db_press",
            name = "Flat Dumbbell Press",
            equipment = "DUMBBELL",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "FRONT_DELTS,TRICEPS",
            creatorTags = "#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_flat_smith_press",
            name = "Flat Smith Machine Bench Press",
            equipment = "SMITH_MACHINE",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "TRICEPS,FRONT_DELTS",
            creatorTags = "#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_seated_machine_chest_press",
            name = "Seated Machine Chest Press",
            equipment = "MACHINE",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "TRICEPS,FRONT_DELTS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_standing_cable_crossover",
            name = "Standing Middle Cable Crossover",
            equipment = "CABLE",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "FRONT_DELTS",
            creatorTags = "#Isolation,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_pec_deck_flye",
            name = "Pec Deck Flye (Machine Flye)",
            equipment = "MACHINE",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "UPPER_CHEST",
            creatorTags = "#DrMike,#JeffNippard,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_flat_db_flye",
            name = "Flat Dumbbell Flye",
            equipment = "DUMBBELL",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "FRONT_DELTS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_pushup",
            name = "Standard Push-Up",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "TRICEPS,FRONT_DELTS,UPPER_ABS",
            creatorTags = "#Calisthenics,#Bodyweight"
        ),
        ExerciseEntity(
            id = "ex_svend_press",
            name = "Standing Plate Svend Press",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "FRONT_DELTS",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_floor_press_bb",
            name = "Barbell Floor Press",
            equipment = "BARBELL",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "TRICEPS,FRONT_DELTS",
            creatorTags = "#Strength"
        ),
        ExerciseEntity(
            id = "ex_floor_press_db",
            name = "Dumbbell Floor Press",
            equipment = "DUMBBELL",
            primarySubMuscle = "MID_CHEST",
            secondarySubMuscles = "TRICEPS,FRONT_DELTS",
            creatorTags = "#Strength"
        ),

        // ==========================================
        // CHEST: LOWER CHEST (Abdominal Head)
        // ==========================================
        ExerciseEntity(
            id = "ex_decline_bb_bench",
            name = "Decline Barbell Bench Press",
            equipment = "BARBELL",
            primarySubMuscle = "LOWER_CHEST",
            secondarySubMuscles = "TRICEPS,MID_CHEST",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_decline_db_press",
            name = "Decline Dumbbell Press",
            equipment = "DUMBBELL",
            primarySubMuscle = "LOWER_CHEST",
            secondarySubMuscles = "TRICEPS,MID_CHEST",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_chest_dips",
            name = "Chest Dips (Forward Lean)",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LOWER_CHEST",
            secondarySubMuscles = "TRICEPS,FRONT_DELTS",
            creatorTags = "#JeffNippard,#Compound"
        ),
        ExerciseEntity(
            id = "ex_high_to_low_cable_flye",
            name = "High-to-Low Cable Flye",
            equipment = "CABLE",
            primarySubMuscle = "LOWER_CHEST",
            secondarySubMuscles = "MID_CHEST",
            creatorTags = "#JeffNippard,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_decline_smith_press",
            name = "Decline Smith Machine Press",
            equipment = "SMITH_MACHINE",
            primarySubMuscle = "LOWER_CHEST",
            secondarySubMuscles = "TRICEPS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_decline_machine_press",
            name = "Decline Chest Press Machine",
            equipment = "MACHINE",
            primarySubMuscle = "LOWER_CHEST",
            secondarySubMuscles = "TRICEPS",
            creatorTags = "#Machine"
        ),
        ExerciseEntity(
            id = "ex_incline_pushup",
            name = "Incline Push-Up (Hands Elevated)",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LOWER_CHEST",
            secondarySubMuscles = "TRICEPS",
            creatorTags = "#Calisthenics"
        ),

        // ==========================================
        // SHOULDERS: FRONT DELTS (Anterior Deltoid)
        // ==========================================
        ExerciseEntity(
            id = "ex_standing_ohp_bb",
            name = "Standing Barbell Overhead Press (OHP)",
            equipment = "BARBELL",
            primarySubMuscle = "FRONT_DELTS",
            secondarySubMuscles = "TRICEPS,UPPER_CHEST,UPPER_BACK_TRAPS",
            creatorTags = "#Compound,#Strength,#JeffNippard"
        ),
        ExerciseEntity(
            id = "ex_seated_db_shoulder_press",
            name = "Seated Dumbbell Shoulder Press",
            equipment = "DUMBBELL",
            primarySubMuscle = "FRONT_DELTS",
            secondarySubMuscles = "TRICEPS,SIDE_DELTS",
            creatorTags = "#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_seated_smith_shoulder_press",
            name = "Seated Smith Machine Overhead Press",
            equipment = "SMITH_MACHINE",
            primarySubMuscle = "FRONT_DELTS",
            secondarySubMuscles = "TRICEPS,UPPER_CHEST",
            creatorTags = "#DrMike"
        ),
        ExerciseEntity(
            id = "ex_arnold_press",
            name = "Arnold Press",
            equipment = "DUMBBELL",
            primarySubMuscle = "FRONT_DELTS",
            secondarySubMuscles = "SIDE_DELTS,TRICEPS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_machine_shoulder_press",
            name = "Machine Overhead Shoulder Press",
            equipment = "MACHINE",
            primarySubMuscle = "FRONT_DELTS",
            secondarySubMuscles = "TRICEPS",
            creatorTags = "#Machine,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_db_front_raise",
            name = "Dumbbell Front Raise",
            equipment = "DUMBBELL",
            primarySubMuscle = "FRONT_DELTS",
            secondarySubMuscles = "SIDE_DELTS",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_cable_front_raise",
            name = "Cable Front Raise",
            equipment = "CABLE",
            primarySubMuscle = "FRONT_DELTS",
            secondarySubMuscles = "UPPER_CHEST",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_kb_clean_and_press",
            name = "Kettlebell Clean and Press",
            equipment = "KETTLEBELL",
            primarySubMuscle = "FRONT_DELTS",
            secondarySubMuscles = "TRICEPS,GLUTES,HAMSTRINGS",
            creatorTags = "#Kettlebell,#Functional"
        ),
        ExerciseEntity(
            id = "ex_pike_pushup",
            name = "Pike Push-Up",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "FRONT_DELTS",
            secondarySubMuscles = "TRICEPS,UPPER_BACK_TRAPS",
            creatorTags = "#Calisthenics"
        ),

        // ==========================================
        // SHOULDERS: SIDE DELTS (Lateral Deltoid)
        // ==========================================
        ExerciseEntity(
            id = "ex_db_lateral_raise",
            name = "Standing Dumbbell Lateral Raise",
            equipment = "DUMBBELL",
            primarySubMuscle = "SIDE_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS",
            creatorTags = "#JeffNippard,#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_cable_lateral_raise",
            name = "Cable Lateral Raise (Behind/Front)",
            equipment = "CABLE",
            primarySubMuscle = "SIDE_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS",
            creatorTags = "#JeffNippard,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_seated_db_lateral_raise",
            name = "Seated Dumbbell Lateral Raise",
            equipment = "DUMBBELL",
            primarySubMuscle = "SIDE_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS",
            creatorTags = "#DrMike,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_machine_lateral_raise",
            name = "Machine Lateral Raise",
            equipment = "MACHINE",
            primarySubMuscle = "SIDE_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS",
            creatorTags = "#Hypertrophy,#Machine"
        ),
        ExerciseEntity(
            id = "ex_incline_lean_lateral_raise",
            name = "Incline Bench Dumbbell Lateral Raise",
            equipment = "DUMBBELL",
            primarySubMuscle = "SIDE_DELTS",
            secondarySubMuscles = "",
            creatorTags = "#JeffNippard"
        ),
        ExerciseEntity(
            id = "ex_upright_row_bb",
            name = "Wide-Grip Barbell Upright Row",
            equipment = "BARBELL",
            primarySubMuscle = "SIDE_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS,BICEPS",
            creatorTags = "#AthleanX"
        ),
        ExerciseEntity(
            id = "ex_upright_row_cable",
            name = "Cable Upright Row",
            equipment = "CABLE",
            primarySubMuscle = "SIDE_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS,BICEPS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_upright_row_db",
            name = "Dumbbell Upright Row",
            equipment = "DUMBBELL",
            primarySubMuscle = "SIDE_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS",
            creatorTags = "#Hypertrophy"
        ),

        // ==========================================
        // SHOULDERS: REAR DELTS (Posterior Deltoid)
        // ==========================================
        ExerciseEntity(
            id = "ex_face_pull",
            name = "Rope Cable Face Pull",
            equipment = "CABLE",
            primarySubMuscle = "REAR_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS,SIDE_DELTS",
            creatorTags = "#AthleanX,#JeffNippard,#Posture"
        ),
        ExerciseEntity(
            id = "ex_reverse_pec_deck",
            name = "Reverse Pec Deck (Rear Delt Flye)",
            equipment = "MACHINE",
            primarySubMuscle = "REAR_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS",
            creatorTags = "#DrMike,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_bent_over_db_rear_flye",
            name = "Bent-Over Dumbbell Rear Delt Flye",
            equipment = "DUMBBELL",
            primarySubMuscle = "REAR_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_cable_rear_delt_crossover",
            name = "Cable Rear Delt Crossover",
            equipment = "CABLE",
            primarySubMuscle = "REAR_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS",
            creatorTags = "#JeffNippard,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_incline_chest_supported_rear_flye",
            name = "Incline Bench Chest-Supported Rear Flye",
            equipment = "DUMBBELL",
            primarySubMuscle = "REAR_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS",
            creatorTags = "#DrMike"
        ),
        ExerciseEntity(
            id = "ex_band_pull_apart",
            name = "Band Pull-Apart",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "REAR_DELTS",
            secondarySubMuscles = "UPPER_BACK_TRAPS",
            creatorTags = "#Mobility,#AthleanX"
        ),

        // ==========================================
        // BACK: LATS (Latissimus Dorsi — Width)
        // ==========================================
        ExerciseEntity(
            id = "ex_lat_pulldown_wide",
            name = "Wide-Grip Lat Pulldown",
            equipment = "CABLE",
            primarySubMuscle = "LATS",
            secondarySubMuscles = "BICEPS,UPPER_BACK_TRAPS",
            creatorTags = "#JeffNippard,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_lat_pulldown_close",
            name = "Close-Grip V-Bar Lat Pulldown",
            equipment = "CABLE",
            primarySubMuscle = "LATS",
            secondarySubMuscles = "BICEPS,UPPER_BACK_TRAPS",
            creatorTags = "#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_pullup",
            name = "Standard Pull-Up (Overhand)",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LATS",
            secondarySubMuscles = "BICEPS,UPPER_BACK_TRAPS,FOREARMS",
            creatorTags = "#Calisthenics,#Compound,#Strength"
        ),
        ExerciseEntity(
            id = "ex_chinup",
            name = "Chin-Up (Underhand)",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LATS",
            secondarySubMuscles = "BICEPS,FOREARMS",
            creatorTags = "#Compound,#Biceps"
        ),
        ExerciseEntity(
            id = "ex_single_arm_db_row",
            name = "Single-Arm Dumbbell Row",
            equipment = "DUMBBELL",
            primarySubMuscle = "LATS",
            secondarySubMuscles = "BICEPS,UPPER_BACK_TRAPS",
            creatorTags = "#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_straight_arm_cable_pulldown",
            name = "Straight-Arm Cable Pulldown",
            equipment = "CABLE",
            primarySubMuscle = "LATS",
            secondarySubMuscles = "TRICEPS",
            creatorTags = "#Isolation,#JeffNippard"
        ),
        ExerciseEntity(
            id = "ex_single_arm_cable_lat_row",
            name = "Half-Kneeling Single-Arm Lat Cable Row",
            equipment = "CABLE",
            primarySubMuscle = "LATS",
            secondarySubMuscles = "BICEPS",
            creatorTags = "#JeffNippard"
        ),
        ExerciseEntity(
            id = "ex_neutral_grip_pulldown",
            name = "Neutral-Grip Lat Pulldown",
            equipment = "CABLE",
            primarySubMuscle = "LATS",
            secondarySubMuscles = "BICEPS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_machine_lat_pulldown",
            name = "Plate-Loaded Iso-Lateral Pulldown",
            equipment = "MACHINE",
            primarySubMuscle = "LATS",
            secondarySubMuscles = "BICEPS",
            creatorTags = "#Machine,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_db_pullover",
            name = "Dumbbell Cross-Bench Pullover",
            equipment = "DUMBBELL",
            primarySubMuscle = "LATS",
            secondarySubMuscles = "UPPER_CHEST,TRICEPS",
            creatorTags = "#OldSchool"
        ),

        // ==========================================
        // BACK: UPPER BACK & TRAPS (Rhomboids / Trapezius)
        // ==========================================
        ExerciseEntity(
            id = "ex_barbell_bent_over_row",
            name = "Barbell Bent-Over Row",
            equipment = "BARBELL",
            primarySubMuscle = "UPPER_BACK_TRAPS",
            secondarySubMuscles = "LATS,LOWER_BACK,BICEPS",
            creatorTags = "#Compound,#Strength,#JeffNippard"
        ),
        ExerciseEntity(
            id = "ex_chest_supported_tbar_row",
            name = "Chest-Supported T-Bar Row",
            equipment = "MACHINE",
            primarySubMuscle = "UPPER_BACK_TRAPS",
            secondarySubMuscles = "LATS,BICEPS,REAR_DELTS",
            creatorTags = "#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_seated_cable_row_wide",
            name = "Wide-Grip Seated Cable Row",
            equipment = "CABLE",
            primarySubMuscle = "UPPER_BACK_TRAPS",
            secondarySubMuscles = "REAR_DELTS,BICEPS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_bb_shrugs",
            name = "Barbell Shrugs",
            equipment = "BARBELL",
            primarySubMuscle = "UPPER_BACK_TRAPS",
            secondarySubMuscles = "FOREARMS",
            creatorTags = "#Isolation,#Traps"
        ),
        ExerciseEntity(
            id = "ex_db_shrugs",
            name = "Dumbbell Shrugs",
            equipment = "DUMBBELL",
            primarySubMuscle = "UPPER_BACK_TRAPS",
            secondarySubMuscles = "FOREARMS",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_smith_machine_shrugs",
            name = "Smith Machine Shrugs (Behind/Front)",
            equipment = "SMITH_MACHINE",
            primarySubMuscle = "UPPER_BACK_TRAPS",
            secondarySubMuscles = "FOREARMS",
            creatorTags = "#Traps"
        ),
        ExerciseEntity(
            id = "ex_incline_chest_supported_db_row",
            name = "Incline Chest-Supported Dumbbell Row",
            equipment = "DUMBBELL",
            primarySubMuscle = "UPPER_BACK_TRAPS",
            secondarySubMuscles = "REAR_DELTS,BICEPS",
            creatorTags = "#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_pendlay_row",
            name = "Pendlay Barbell Row",
            equipment = "BARBELL",
            primarySubMuscle = "UPPER_BACK_TRAPS",
            secondarySubMuscles = "LATS,LOWER_BACK",
            creatorTags = "#Strength,#Power"
        ),
        ExerciseEntity(
            id = "ex_cable_kelso_shrug",
            name = "Cable Incline Kelso Shrug",
            equipment = "CABLE",
            primarySubMuscle = "UPPER_BACK_TRAPS",
            secondarySubMuscles = "",
            creatorTags = "#DrMike,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_rack_pull",
            name = "Above-the-Knee Barbell Rack Pull",
            equipment = "BARBELL",
            primarySubMuscle = "UPPER_BACK_TRAPS",
            secondarySubMuscles = "LOWER_BACK,GLUTES,FOREARMS",
            creatorTags = "#Strength"
        ),

        // ==========================================
        // BACK: LOWER BACK (Erector Spinae)
        // ==========================================
        ExerciseEntity(
            id = "ex_conventional_deadlift",
            name = "Conventional Barbell Deadlift",
            equipment = "BARBELL",
            primarySubMuscle = "LOWER_BACK",
            secondarySubMuscles = "GLUTES,HAMSTRINGS,UPPER_BACK_TRAPS,FOREARMS",
            creatorTags = "#Strength,#Compound,#JeffNippard"
        ),
        ExerciseEntity(
            id = "ex_barbell_good_morning",
            name = "Barbell Good Morning",
            equipment = "BARBELL",
            primarySubMuscle = "LOWER_BACK",
            secondarySubMuscles = "HAMSTRINGS,GLUTES",
            creatorTags = "#Strength,#PosteriorChain"
        ),
        ExerciseEntity(
            id = "ex_hyperextension_back",
            name = "45-Degree Back Hyperextension",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LOWER_BACK",
            secondarySubMuscles = "GLUTES,HAMSTRINGS",
            creatorTags = "#PosteriorChain"
        ),
        ExerciseEntity(
            id = "ex_weighted_hyperextension",
            name = "Weighted Back Hyperextension (Plate/DB)",
            equipment = "DUMBBELL",
            primarySubMuscle = "LOWER_BACK",
            secondarySubMuscles = "GLUTES,HAMSTRINGS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_superman",
            name = "Floor Superman Hold",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LOWER_BACK",
            secondarySubMuscles = "GLUTES",
            creatorTags = "#Rehab,#Bodyweight"
        ),
        ExerciseEntity(
            id = "ex_bird_dog",
            name = "Quadruped Bird Dog",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LOWER_BACK",
            secondarySubMuscles = "GLUTES,UPPER_ABS",
            creatorTags = "#Rehab,#Mobility"
        ),

        // ==========================================
        // ARMS: BICEPS (Biceps Brachii & Brachialis)
        // ==========================================
        ExerciseEntity(
            id = "ex_barbell_curl",
            name = "Standing Barbell Bicep Curl",
            equipment = "BARBELL",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "FOREARMS",
            creatorTags = "#Hypertrophy,#Classic"
        ),
        ExerciseEntity(
            id = "ex_ez_bar_preacher_curl",
            name = "EZ-Bar Preacher Curl",
            equipment = "BARBELL",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "FOREARMS",
            creatorTags = "#DrMike,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_incline_db_curl",
            name = "Incline Dumbbell Bicep Curl",
            equipment = "DUMBBELL",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "FOREARMS",
            creatorTags = "#JeffNippard,#LongHead"
        ),
        ExerciseEntity(
            id = "ex_db_hammer_curl",
            name = "Dumbbell Hammer Curl",
            equipment = "DUMBBELL",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "FOREARMS",
            creatorTags = "#Brachialis,#Forearms"
        ),
        ExerciseEntity(
            id = "ex_rope_cable_hammer_curl",
            name = "Rope Cable Hammer Curl",
            equipment = "CABLE",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "FOREARMS",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_standing_db_alternating_curl",
            name = "Standing Alternating Dumbbell Curl",
            equipment = "DUMBBELL",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "FOREARMS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_concentration_curl",
            name = "Seated Dumbbell Concentration Curl",
            equipment = "DUMBBELL",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "",
            creatorTags = "#Classic,#Peak"
        ),
        ExerciseEntity(
            id = "ex_bayesian_cable_curl",
            name = "Bayesian Behind-the-Back Cable Curl",
            equipment = "CABLE",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "",
            creatorTags = "#JeffNippard,#StretchMediated"
        ),
        ExerciseEntity(
            id = "ex_spider_curl",
            name = "Incline Bench Spider Curl",
            equipment = "DUMBBELL",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "",
            creatorTags = "#ShortHead,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_machine_bicep_curl",
            name = "Machine Preacher Curl",
            equipment = "MACHINE",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "",
            creatorTags = "#Machine,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_high_cable_bicep_curl",
            name = "Overhead High Cable Bicep Curl",
            equipment = "CABLE",
            primarySubMuscle = "BICEPS",
            secondarySubMuscles = "",
            creatorTags = "#Isolation"
        ),

        // ==========================================
        // ARMS: TRICEPS (Triceps Brachii)
        // ==========================================
        ExerciseEntity(
            id = "ex_rope_tricep_pushdown",
            name = "Rope Cable Tricep Pushdown",
            equipment = "CABLE",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "",
            creatorTags = "#JeffNippard,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_straight_bar_pushdown",
            name = "Straight Bar Tricep Pushdown",
            equipment = "CABLE",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "",
            creatorTags = "#Strength"
        ),
        ExerciseEntity(
            id = "ex_skull_crushers_ez",
            name = "EZ-Bar Lying Tricep Extension (Skull Crusher)",
            equipment = "BARBELL",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "",
            creatorTags = "#Hypertrophy,#LongHead"
        ),
        ExerciseEntity(
            id = "ex_overhead_cable_tricep_ext",
            name = "Overhead Rope Cable Tricep Extension",
            equipment = "CABLE",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "",
            creatorTags = "#JeffNippard,#LongHead,#StretchMediated"
        ),
        ExerciseEntity(
            id = "ex_seated_overhead_db_ext",
            name = "Seated Two-Hand Dumbbell Overhead Extension",
            equipment = "DUMBBELL",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "",
            creatorTags = "#LongHead"
        ),
        ExerciseEntity(
            id = "ex_close_grip_bb_bench",
            name = "Close-Grip Barbell Bench Press",
            equipment = "BARBELL",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "MID_CHEST,FRONT_DELTS",
            creatorTags = "#Compound,#Strength"
        ),
        ExerciseEntity(
            id = "ex_parallel_bar_dips_triceps",
            name = "Tricep Parallel Bar Dips (Upright)",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "LOWER_CHEST,FRONT_DELTS",
            creatorTags = "#Compound,#Bodyweight"
        ),
        ExerciseEntity(
            id = "ex_bench_dips",
            name = "Bench Dips",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "FRONT_DELTS",
            creatorTags = "#Bodyweight"
        ),
        ExerciseEntity(
            id = "ex_single_arm_cable_kickback",
            name = "Single-Arm Cable Tricep Kickback",
            equipment = "CABLE",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "",
            creatorTags = "#DrMike,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_diamond_pushups",
            name = "Diamond Push-Up",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "MID_CHEST,FRONT_DELTS",
            creatorTags = "#Calisthenics"
        ),
        ExerciseEntity(
            id = "ex_jm_press",
            name = "JM Press (Barbell / Smith)",
            equipment = "BARBELL",
            primarySubMuscle = "TRICEPS",
            secondarySubMuscles = "MID_CHEST",
            creatorTags = "#Powerlifting"
        ),

        // ==========================================
        // ARMS: FOREARMS (Brachioradialis / Wrist Flexors & Extensors)
        // ==========================================
        ExerciseEntity(
            id = "ex_bb_wrist_curl",
            name = "Seated Barbell Wrist Curl (Palms Up)",
            equipment = "BARBELL",
            primarySubMuscle = "FOREARMS",
            secondarySubMuscles = "",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_bb_reverse_wrist_curl",
            name = "Seated Barbell Reverse Wrist Curl (Palms Down)",
            equipment = "BARBELL",
            primarySubMuscle = "FOREARMS",
            secondarySubMuscles = "",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_db_wrist_curl",
            name = "Dumbbell Wrist Flexion Curl",
            equipment = "DUMBBELL",
            primarySubMuscle = "FOREARMS",
            secondarySubMuscles = "",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_reverse_grip_bb_curl",
            name = "Standing Reverse-Grip Barbell Curl",
            equipment = "BARBELL",
            primarySubMuscle = "FOREARMS",
            secondarySubMuscles = "BICEPS",
            creatorTags = "#Brachioradialis"
        ),
        ExerciseEntity(
            id = "ex_farmers_walk_db",
            name = "Dumbbell Farmer's Walk",
            equipment = "DUMBBELL",
            primarySubMuscle = "FOREARMS",
            secondarySubMuscles = "UPPER_BACK_TRAPS,UPPER_ABS",
            creatorTags = "#Grip,#Functional"
        ),
        ExerciseEntity(
            id = "ex_dead_hang",
            name = "Pull-Up Bar Dead Hang",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "FOREARMS",
            secondarySubMuscles = "LATS",
            creatorTags = "#Grip,#Decompression"
        ),
        ExerciseEntity(
            id = "ex_wrist_roller",
            name = "Standing Wrist Roller",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "FOREARMS",
            secondarySubMuscles = "FRONT_DELTS",
            creatorTags = "#Grip,#Isolation"
        ),

        // ==========================================
        // LEGS: QUADS (Quadriceps Femoris)
        // ==========================================
        ExerciseEntity(
            id = "ex_bb_back_squat",
            name = "Barbell Back Squat",
            equipment = "BARBELL",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "GLUTES,LOWER_BACK,HAMSTRINGS",
            creatorTags = "#Compound,#Strength,#JeffNippard"
        ),
        ExerciseEntity(
            id = "ex_bb_front_squat",
            name = "Barbell Front Squat",
            equipment = "BARBELL",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "UPPER_ABS,GLUTES,UPPER_BACK_TRAPS",
            creatorTags = "#Strength,#Quads"
        ),
        ExerciseEntity(
            id = "ex_leg_press",
            name = "45-Degree Incline Leg Press",
            equipment = "MACHINE",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "GLUTES,HAMSTRINGS",
            creatorTags = "#Hypertrophy,#DrMike"
        ),
        ExerciseEntity(
            id = "ex_hack_squat",
            name = "Machine Hack Squat",
            equipment = "MACHINE",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "GLUTES",
            creatorTags = "#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_leg_extension",
            name = "Seated Leg Extension Machine",
            equipment = "MACHINE",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "",
            creatorTags = "#JeffNippard,#Isolation,#RectusFemoris"
        ),
        ExerciseEntity(
            id = "ex_bulgarian_split_squat_db",
            name = "Dumbbell Bulgarian Split Squat (Quad Bias)",
            equipment = "DUMBBELL",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "GLUTES,HAMSTRINGS",
            creatorTags = "#Hypertrophy,#JeffNippard"
        ),
        ExerciseEntity(
            id = "ex_smith_machine_squat",
            name = "Smith Machine Squat (Feet Forward)",
            equipment = "SMITH_MACHINE",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "GLUTES",
            creatorTags = "#DrMike"
        ),
        ExerciseEntity(
            id = "ex_walking_lunges_db",
            name = "Walking Dumbbell Lunges",
            equipment = "DUMBBELL",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "GLUTES,HAMSTRINGS",
            creatorTags = "#Conditioning"
        ),
        ExerciseEntity(
            id = "ex_goblet_squat_db",
            name = "Dumbbell Goblet Squat",
            equipment = "DUMBBELL",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "GLUTES,UPPER_ABS",
            creatorTags = "#Beginner"
        ),
        ExerciseEntity(
            id = "ex_sissy_squat",
            name = "Bodyweight Sissy Squat",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "",
            creatorTags = "#Calisthenics,#StretchMediated"
        ),
        ExerciseEntity(
            id = "ex_step_ups_db",
            name = "Dumbbell Box Step-Up",
            equipment = "DUMBBELL",
            primarySubMuscle = "QUADS",
            secondarySubMuscles = "GLUTES",
            creatorTags = "#Unilateral"
        ),

        // ==========================================
        // LEGS: HAMSTRINGS (Biceps Femoris / Semitendinosus)
        // ==========================================
        ExerciseEntity(
            id = "ex_romanian_deadlift_bb",
            name = "Barbell Romanian Deadlift (RDL)",
            equipment = "BARBELL",
            primarySubMuscle = "HAMSTRINGS",
            secondarySubMuscles = "GLUTES,LOWER_BACK",
            creatorTags = "#JeffNippard,#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_romanian_deadlift_db",
            name = "Dumbbell Romanian Deadlift (RDL)",
            equipment = "DUMBBELL",
            primarySubMuscle = "HAMSTRINGS",
            secondarySubMuscles = "GLUTES,LOWER_BACK",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_seated_leg_curl",
            name = "Seated Leg Curl Machine",
            equipment = "MACHINE",
            primarySubMuscle = "HAMSTRINGS",
            secondarySubMuscles = "CALVES",
            creatorTags = "#JeffNippard,#StretchMediated,#DrMike"
        ),
        ExerciseEntity(
            id = "ex_lying_leg_curl",
            name = "Lying Leg Curl Machine",
            equipment = "MACHINE",
            primarySubMuscle = "HAMSTRINGS",
            secondarySubMuscles = "",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_nordic_curl",
            name = "Nordic Hamstring Curl",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "HAMSTRINGS",
            secondarySubMuscles = "GLUTES",
            creatorTags = "#Calisthenics,#InjuryPrevention"
        ),
        ExerciseEntity(
            id = "ex_single_leg_rdl_db",
            name = "Single-Leg Dumbbell RDL",
            equipment = "DUMBBELL",
            primarySubMuscle = "HAMSTRINGS",
            secondarySubMuscles = "GLUTES,OBLIQUES",
            creatorTags = "#Unilateral,#Balance"
        ),
        ExerciseEntity(
            id = "ex_smith_machine_rdl",
            name = "Smith Machine Romanian Deadlift",
            equipment = "SMITH_MACHINE",
            primarySubMuscle = "HAMSTRINGS",
            secondarySubMuscles = "GLUTES,LOWER_BACK",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_swiss_ball_hamstring_curl",
            name = "Swiss Ball Leg Curl",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "HAMSTRINGS",
            secondarySubMuscles = "GLUTES",
            creatorTags = "#Rehab"
        ),

        // ==========================================
        // LEGS: GLUTES (Gluteus Maximus / Medius)
        // ==========================================
        ExerciseEntity(
            id = "ex_barbell_hip_thrust",
            name = "Barbell Hip Thrust",
            equipment = "BARBELL",
            primarySubMuscle = "GLUTES",
            secondarySubMuscles = "HAMSTRINGS,QUADS",
            creatorTags = "#JeffNippard,#Hypertrophy,#BretContreras"
        ),
        ExerciseEntity(
            id = "ex_smith_machine_hip_thrust",
            name = "Smith Machine Hip Thrust",
            equipment = "SMITH_MACHINE",
            primarySubMuscle = "GLUTES",
            secondarySubMuscles = "HAMSTRINGS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_sumo_deadlift",
            name = "Sumo Barbell Deadlift",
            equipment = "BARBELL",
            primarySubMuscle = "GLUTES",
            secondarySubMuscles = "QUADS,HAMSTRINGS,LOWER_BACK",
            creatorTags = "#Powerlifting,#Strength"
        ),
        ExerciseEntity(
            id = "ex_cable_pull_through",
            name = "Rope Cable Pull-Through",
            equipment = "CABLE",
            primarySubMuscle = "GLUTES",
            secondarySubMuscles = "HAMSTRINGS,LOWER_BACK",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_cable_glute_kickback",
            name = "Standing Cable Glute Kickback",
            equipment = "CABLE",
            primarySubMuscle = "GLUTES",
            secondarySubMuscles = "HAMSTRINGS",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_seated_hip_abduction_machine",
            name = "Seated Hip Abduction Machine",
            equipment = "MACHINE",
            primarySubMuscle = "GLUTES",
            secondarySubMuscles = "",
            creatorTags = "#GluteMedius,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_glute_bridge_bodyweight",
            name = "Floor Glute Bridge",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "GLUTES",
            secondarySubMuscles = "HAMSTRINGS",
            creatorTags = "#Warmup,#Bodyweight"
        ),
        ExerciseEntity(
            id = "ex_bulgarian_split_squat_glute",
            name = "Dumbbell Bulgarian Split Squat (Glute Lean)",
            equipment = "DUMBBELL",
            primarySubMuscle = "GLUTES",
            secondarySubMuscles = "HAMSTRINGS,QUADS",
            creatorTags = "#Hypertrophy"
        ),

        // ==========================================
        // LEGS: CALVES (Gastrocnemius & Soleus)
        // ==========================================
        ExerciseEntity(
            id = "ex_standing_calf_raise_machine",
            name = "Standing Machine Calf Raise",
            equipment = "MACHINE",
            primarySubMuscle = "CALVES",
            secondarySubMuscles = "",
            creatorTags = "#JeffNippard,#Gastrocnemius"
        ),
        ExerciseEntity(
            id = "ex_seated_calf_raise_machine",
            name = "Seated Machine Calf Raise",
            equipment = "MACHINE",
            primarySubMuscle = "CALVES",
            secondarySubMuscles = "",
            creatorTags = "#Soleus,#Isolation"
        ),
        ExerciseEntity(
            id = "ex_leg_press_calf_press",
            name = "Leg Press Calf Press",
            equipment = "MACHINE",
            primarySubMuscle = "CALVES",
            secondarySubMuscles = "",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_single_leg_db_calf_raise",
            name = "Single-Leg Dumbbell Calf Raise (Step)",
            equipment = "DUMBBELL",
            primarySubMuscle = "CALVES",
            secondarySubMuscles = "",
            creatorTags = "#Unilateral"
        ),
        ExerciseEntity(
            id = "ex_smith_machine_standing_calf_raise",
            name = "Smith Machine Standing Calf Raise",
            equipment = "SMITH_MACHINE",
            primarySubMuscle = "CALVES",
            secondarySubMuscles = "",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_donkey_calf_raise",
            name = "Donkey Calf Raise",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "CALVES",
            secondarySubMuscles = "",
            creatorTags = "#OldSchool"
        ),

        // ==========================================
        // CORE: UPPER ABS (Rectus Abdominis Superior)
        // ==========================================
        ExerciseEntity(
            id = "ex_cable_kneeling_crunch",
            name = "Kneeling Cable Rope Crunch",
            equipment = "CABLE",
            primarySubMuscle = "UPPER_ABS",
            secondarySubMuscles = "LOWER_ABS",
            creatorTags = "#JeffNippard,#DrMike,#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_decline_crunch_weighted",
            name = "Decline Bench Weighted Crunch",
            equipment = "DUMBBELL",
            primarySubMuscle = "UPPER_ABS",
            secondarySubMuscles = "LOWER_ABS",
            creatorTags = "#Strength"
        ),
        ExerciseEntity(
            id = "ex_ab_roller_wheel",
            name = "Ab Wheel Rollout",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "UPPER_ABS",
            secondarySubMuscles = "LOWER_ABS,LATS",
            creatorTags = "#CoreStability,#AthleanX"
        ),
        ExerciseEntity(
            id = "ex_standard_floor_crunch",
            name = "Floor Crunch",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "UPPER_ABS",
            secondarySubMuscles = "",
            creatorTags = "#Bodyweight"
        ),
        ExerciseEntity(
            id = "ex_machine_ab_crunch",
            name = "Seated Machine Abdominal Crunch",
            equipment = "MACHINE",
            primarySubMuscle = "UPPER_ABS",
            secondarySubMuscles = "LOWER_ABS",
            creatorTags = "#Machine"
        ),
        ExerciseEntity(
            id = "ex_plank_hold",
            name = "Forearm Plank Hold",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "UPPER_ABS",
            secondarySubMuscles = "LOWER_ABS,OBLIQUES,LOWER_BACK",
            creatorTags = "#Stability"
        ),

        // ==========================================
        // CORE: LOWER ABS (Rectus Abdominis Inferior)
        // ==========================================
        ExerciseEntity(
            id = "ex_hanging_leg_raise",
            name = "Hanging Straight Leg Raise",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LOWER_ABS",
            secondarySubMuscles = "UPPER_ABS,FOREARMS",
            creatorTags = "#JeffNippard,#Calisthenics"
        ),
        ExerciseEntity(
            id = "ex_hanging_knee_raise",
            name = "Hanging Knee Raise (Captains Chair / Bar)",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LOWER_ABS",
            secondarySubMuscles = "UPPER_ABS",
            creatorTags = "#Bodyweight"
        ),
        ExerciseEntity(
            id = "ex_reverse_crunch",
            name = "Incline Bench Reverse Crunch",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LOWER_ABS",
            secondarySubMuscles = "UPPER_ABS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_lying_leg_raise",
            name = "Lying Floor Leg Raise",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LOWER_ABS",
            secondarySubMuscles = "UPPER_ABS",
            creatorTags = "#Bodyweight"
        ),
        ExerciseEntity(
            id = "ex_dragon_flag",
            name = "Dragon Flag (Bruce Lee)",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "LOWER_ABS",
            secondarySubMuscles = "UPPER_ABS,LATS",
            creatorTags = "#Advanced,#Calisthenics"
        ),
        ExerciseEntity(
            id = "ex_cable_reverse_crunch",
            name = "Cable Ankle-Strap Reverse Crunch",
            equipment = "CABLE",
            primarySubMuscle = "LOWER_ABS",
            secondarySubMuscles = "UPPER_ABS",
            creatorTags = "#DrMike,#Isolation"
        ),

        // ==========================================
        // CORE: OBLIQUES (External & Internal Obliques)
        // ==========================================
        ExerciseEntity(
            id = "ex_cable_woodchopper_high_low",
            name = "Standing Cable Woodchopper (High-to-Low)",
            equipment = "CABLE",
            primarySubMuscle = "OBLIQUES",
            secondarySubMuscles = "UPPER_ABS",
            creatorTags = "#AthleanX,#Rotational"
        ),
        ExerciseEntity(
            id = "ex_cable_woodchopper_low_high",
            name = "Standing Cable Woodchopper (Low-to-High)",
            equipment = "CABLE",
            primarySubMuscle = "OBLIQUES",
            secondarySubMuscles = "UPPER_ABS",
            creatorTags = "#AthleanX,#Rotational"
        ),
        ExerciseEntity(
            id = "ex_russian_twist_weighted",
            name = "Weighted Russian Twist",
            equipment = "DUMBBELL",
            primarySubMuscle = "OBLIQUES",
            secondarySubMuscles = "UPPER_ABS,LOWER_ABS",
            creatorTags = "#Hypertrophy"
        ),
        ExerciseEntity(
            id = "ex_side_plank",
            name = "Side Plank Hold",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "OBLIQUES",
            secondarySubMuscles = "GLUTES",
            creatorTags = "#Stability,#Core"
        ),
        ExerciseEntity(
            id = "ex_hanging_oblique_knee_raise",
            name = "Hanging Oblique Windshield Wiper / Knee Twist",
            equipment = "BODYWEIGHT",
            primarySubMuscle = "OBLIQUES",
            secondarySubMuscles = "LOWER_ABS,FOREARMS",
            creatorTags = "#Calisthenics"
        ),
        ExerciseEntity(
            id = "ex_db_side_bend",
            name = "Single-Dumbbell Standing Side Bend",
            equipment = "DUMBBELL",
            primarySubMuscle = "OBLIQUES",
            secondarySubMuscles = "",
            creatorTags = "#Isolation"
        ),
        ExerciseEntity(
            id = "ex_pallof_press_cable",
            name = "Cable Pallof Press (Anti-Rotation)",
            equipment = "CABLE",
            primarySubMuscle = "OBLIQUES",
            secondarySubMuscles = "UPPER_ABS",
            creatorTags = "#Functional,#Stability"
        )
    )
}
