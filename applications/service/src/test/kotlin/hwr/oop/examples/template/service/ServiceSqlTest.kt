package hwr.oop.examples.template.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import hwr.oop.examples.template.SqlPersistence
import hwr.oop.examples.template.core.GamePersistence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = MOCK)
class ServiceSqlTest {
	
	companion object {
		@Container
		@JvmStatic
		val postgres = PostgreSQLContainer("postgres:17-alpine")
	}
	
	@TestConfiguration
	class Config {
		@Bean
		@Primary
		fun testPersistence(): GamePersistence {
			return SqlPersistence(
				HikariDataSource(HikariConfig().apply {
					jdbcUrl = postgres.jdbcUrl
					username = postgres.username
					password = postgres.password
				})
			)
		}
	}
	
	@Autowired
	private lateinit var webApplicationContext: WebApplicationContext
	
	@Autowired
	private lateinit var objectMapper: ObjectMapper
	
	private lateinit var mockMvc: MockMvc
	
	@BeforeEach
	fun setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
	}
	
	@Test
	fun `start game and then get game`() {
		val startRequest = """
      {
        "playerIds": ["p1", "p2"]
      }
    """.trimIndent()
		
		val startResponse = mockMvc.post("/games") {
			contentType = MediaType.APPLICATION_JSON
			content = startRequest
		}
			.andExpect {
				status { isCreated() }
			}
			.andReturn()
		
		val createdJson: JsonNode = objectMapper.readTree(startResponse.response.contentAsString)
		val gameId = createdJson["gameId"].asText()
		
		val getResponse = mockMvc.get("/games/$gameId") {
			accept = MediaType.APPLICATION_JSON
		}
			.andExpect {
				status { isOk() }
			}
			.andReturn()
		
		val gameJson = objectMapper.readTree(getResponse.response.contentAsString)
		
		assertEquals(gameId, gameJson["gameId"].asText())
		assertEquals("IN_PROGRESS", gameJson["status"].asText())
		assertEquals("p1", gameJson["attackerId"].asText())
		assertEquals("p2", gameJson["defenderId"].asText())
		assertEquals(2, gameJson["playerHands"].size())
	}
	
	@Test
	fun `attack persists state and get game returns updated attack stack`() {
		val startRequest = """
      {
        "playerIds": ["p1", "p2"]
      }
    """.trimIndent()
		
		val startResponse = mockMvc.post("/games") {
			contentType = MediaType.APPLICATION_JSON
			content = startRequest
		}
			.andExpect {
				status { isCreated() }
			}
			.andReturn()
		
		val gameId = objectMapper.readTree(startResponse.response.contentAsString)["gameId"].asText()
		
		val gameBeforeAttack = objectMapper.readTree(
			mockMvc.get("/games/$gameId") {
				accept = MediaType.APPLICATION_JSON
			}
				.andExpect {
					status { isOk() }
				}
				.andReturn()
				.response.contentAsString
		)
		
		val playerHands = gameBeforeAttack["playerHands"]
		val attackerHand = playerHands.first { it["playerId"].asText() == "p1" }
		val attackerCard = attackerHand["cards"][0]
		
		val attackRequest = """
      {
        "playerId": "p1",
        "card": {
          "suit": "${attackerCard["suit"].asText()}",
          "rank": "${attackerCard["rank"].asText()}"
        }
      }
    """.trimIndent()
		
		mockMvc.post("/games/$gameId/attack") {
			contentType = MediaType.APPLICATION_JSON
			content = attackRequest
		}
			.andExpect {
				status { isOk() }
			}
		
		val gameAfterAttack = objectMapper.readTree(
			mockMvc.get("/games/$gameId") {
				accept = MediaType.APPLICATION_JSON
			}
				.andExpect {
					status { isOk() }
				}
				.andReturn()
				.response.contentAsString
		)
		
		assertEquals(1, gameAfterAttack["attackStack"].size())
		assertEquals(
			attackerCard["suit"].asText(),
			gameAfterAttack["attackStack"][0]["attackCard"]["suit"].asText()
		)
		assertEquals(
			attackerCard["rank"].asText(),
			gameAfterAttack["attackStack"][0]["attackCard"]["rank"].asText()
		)
	}
}