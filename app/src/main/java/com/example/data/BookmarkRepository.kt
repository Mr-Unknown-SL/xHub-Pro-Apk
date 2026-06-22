package com.example.data

import kotlinx.coroutines.flow.Flow

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    suspend fun insert(bookmark: BookmarkEntity) {
        bookmarkDao.insertBookmark(bookmark)
    }

    suspend fun delete(id: Int) {
        bookmarkDao.deleteBookmark(id)
    }

    suspend fun deleteAll() {
        bookmarkDao.deleteAllLocalBookmarks()
    }

    suspend fun checkAndSeedDefaults() {
        val count = bookmarkDao.getBookmarkCount()
        if (count == 0) {
            // Seed default bookmarks matching user requirements
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    name = "Pornhub",
                    url = "https://www.pornhub.com",
                    logoChar = "P"
                )
            )
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    name = "XNXX",
                    url = "https://www.xnxx.com",
                    logoChar = "X"
                )
            )
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    name = "xHamster",
                    url = "https://www.xhamster.com",
                    logoChar = "H"
                )
            )
        }
    }
}
