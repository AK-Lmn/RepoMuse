package com.example.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    fun getProject(id: Int): Flow<Project?> = projectDao.getProjectById(id)

    suspend fun insert(project: Project) {
        projectDao.insertProject(project)
    }

    suspend fun update(project: Project) {
        projectDao.updateProject(project)
    }

    suspend fun deleteById(id: Int) {
        projectDao.deleteProjectById(id)
    }
}
