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
            // Robust parsing for GitHub URLs
            // Matches: github.com/owner/repo, https://github.com/owner/repo/, http://github.com/owner/repo.git etc.
            val cleanUrl = repoUrl.trim()
                .removePrefix("https://")
                .removePrefix("http://")
                .split("?")[0] // Remove query params
                .split("#")[0] // Remove fragments
                .removeSuffix("/")

            val regex = Regex("github\\.com/([^/]+)/([^/]+)")
            val matchResult = regex.find(cleanUrl)
            
            if (matchResult == null) {
                return@withContext ""
            }
            
            val owner = matchResult.groupValues[1]
            val repo = matchResult.groupValues[2].removeSuffix(".git")
            
            val request = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "RepoMuse-App") // Good practice for GitHub API
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
