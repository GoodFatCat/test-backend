package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object AuthorService {
    suspend fun addRecord(fullName: String) : AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val authorEntity = AuthorEntity.new {
                this.fullName = fullName
                this.dateOfCreation = DateTime.now();
            }
            return@transaction authorEntity.toRecord()
        }
    }
}