package mobi.sevenwinds.app.author

import mobi.sevenwinds.app.budget.BudgetRecord
import mobi.sevenwinds.app.budget.BudgetTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("author") {
    val fullName = varchar("full_name", 100)
    val dateOfCreation = datetime("date_of_creation")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)
    var fullName by AuthorTable.fullName
    var dateOfCreation by AuthorTable.dateOfCreation

    fun toRecord(): AuthorRecord {
        return AuthorRecord(fullName, dateOfCreation)
    }
}