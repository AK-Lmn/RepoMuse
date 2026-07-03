package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    fun getProject(id: String): Flow<Project?> = projectDao.getProjectById(id)

    suspend fun insert(project: Project) {
        projectDao.insertProject(project)
        syncToCloud(project)
    }

    suspend fun update(project: Project) {
        projectDao.updateProject(project)
        syncToCloud(project)
    }

    suspend fun deleteById(id: String) {
        projectDao.deleteProjectById(id)
        deleteFromCloud(id)
    }
    
    private fun getUserId(): String? = try { FirebaseAuth.getInstance().currentUser?.uid } catch (e: Exception) { null }
    
    private fun getCloudRef(userId: String) = FirebaseFirestore.getInstance()
        .collection("users").document(userId).collection("projects")

    private fun syncToCloud(project: Project) {
        val userId = getUserId() ?: return
        getCloudRef(userId).document(project.id).set(project)
    }

    private fun deleteFromCloud(projectId: String) {
        val userId = getUserId() ?: return
        getCloudRef(userId).document(projectId).delete()
    }
    
    suspend fun syncFromCloud() {
        val userId = getUserId() ?: return
        try {
            val snapshot = getCloudRef(userId).get().await()
            val cloudProjects = snapshot.toObjects(Project::class.java)
            cloudProjects.forEach { project ->
                // Basic strategy: just insert, assuming IDs align or create duplicates for now
                // A better approach would be upsert, which Room's OnConflictStrategy.REPLACE does.
                projectDao.insertProject(project) 
            }
        } catch (e: Exception) {
            // Ignore for MVP
        }
    }
}
