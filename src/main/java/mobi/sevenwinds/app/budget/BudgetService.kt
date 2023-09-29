package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.collections.ArrayList
import kotlin.streams.toList

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            body.authorId?.let { val author = AuthorEntity.findById(it)
            println(author)
            }
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = body.authorId?.let { AuthorEntity.findById(it) }
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .select { BudgetTable.year eq param.year }

            val total = query.count()

            var data = BudgetEntity.wrapRows(query).map { it.toResponse() }

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            data = data.sortedWith(compareBy<BudgetRecord> { it.month }.thenByDescending { it.amount })

            val offset = Math.min(param.offset, data.size)
            val limit = Math.min(offset + param.limit, data.size)

            data = data.subList(offset, limit)

            val listBudgetResponse = ArrayList<BudgetResponse>()

            data.stream().forEach { record ->
                run {
                    val budgetResponse: BudgetResponse
                    if (record.authorId == null) {
                        budgetResponse = BudgetResponse(
                            record.year, record.month, record.amount, record.type,
                            null, null
                        )
                    } else {
                        val author = AuthorEntity.findById(requireNotNull(record.authorId))

                        budgetResponse = BudgetResponse(
                            record.year, record.month, record.amount, record.type,
                            author?.fullName, author?.dateOfCreation
                        )
                    }
                    listBudgetResponse.add(budgetResponse)
                }
            }

            val result = param.fullNameFilter?.let {
                listBudgetResponse.stream()
                    .filter { response ->
                        val regex = ".*" + param.fullNameFilter + ".*".toRegex()
                        response.authorFullName?.let { regex.contains(response.authorFullName.toLowerCase()) }
                            ?: run { false }
                    }
                    .toList()
            } ?: listBudgetResponse

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = result
            )
        }
    }
}