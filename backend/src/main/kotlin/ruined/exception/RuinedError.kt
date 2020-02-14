package ruined.exception

import graphql.ErrorClassification
import graphql.ErrorType
import graphql.GraphQLError
import graphql.language.SourceLocation

/**
 * Error type used to guard exceptions thrown during data fetching.
 */
class RuinedError(
    private val theMessage: String?,
    private val thePath: List<Any>,
    private val theErrorType: ErrorClassification = ErrorType.DataFetchingException
) : GraphQLError {
    override fun getMessage(): String? = theMessage
    override fun getErrorType(): ErrorClassification = theErrorType
    override fun getLocations(): List<SourceLocation> = emptyList()
    override fun getPath(): List<Any> = thePath
}