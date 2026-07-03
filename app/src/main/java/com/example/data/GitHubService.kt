package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object GitHubService {
    private val client = OkHttpClient()

    suspend fun getRepoMetadata(repoUrl: String): String = withContext(Dispatchers.IO) {
        try {
            // Extract owner and repo from URL
            // e.g., https://github.com/owner/repo
            val regex = Regex("github\\.com/([^/]+)/([^/]+)")
            val matchResult = regex.find(repoUrl)
            
            if (matchResult == null) {
                return@withContext ""
            }
            
            val owner = matchResult.groupValues[1]
            val repo = matchResult.groupValues[2].removeSuffix(".git")
            
            val request = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo")
                .header("Accept", "application/vnd.github.v3+json")
                .build()
                
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext ""
                
                val body = response.body?.string() ?: return@withContext ""
                val json = JSONObject(body)
                
                val description = json.optString("description", "No description provided.")
                val language = json.optString("language", "Unknown")
                
                val topicsArray = json.optJSONArray("topics")
                val topics = mutableListOf<String>()
                if (topicsArray != null) {
                    for (i in 0 until topicsArray.length()) {
                        topics.add(topicsArray.getString(i))
                    }
                }
                
                return@withContext "GitHub Repo Description: $description\nPrimary Language: $language\nTopics: ${topics.joinToString(", ")}"
            }
        } catch (e: Exception) {
            return@withContext ""
        }
    }
}
