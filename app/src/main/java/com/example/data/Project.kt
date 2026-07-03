package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val pitch: String = "",
    val problem: String = "",
    val githubUrl: String = "",
    val liveUrl: String = "",
    val features: String = "",
    val techStack: String = "",
    val challenges: String = "",
    val resumeBullets: String = "",
    val caseStudy: String = "",
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
