@WebFluxTest(TaskController::class)
class TaskControllerTest(@Autowired val webClient: WebTestClient) {

    @MockBean
    lateinit var taskService: TaskService

    @Test
    fun `create task returns 200`() {
        val request = TaskRequest("Ctrl Test", "Desc")
        val response = TaskResponse(1, "Ctrl Test", "Desc", TaskStatus.NEW, LocalDateTime.now(), LocalDateTime.now())
        Mockito.`when`(taskService.createTask(request)).thenReturn(Mono.just(response))

        webClient.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.title").isEqualTo("Ctrl Test")
    }

    @Test
    fun `get task by id returns 404`() {
        Mockito.`when`(taskService.getTaskById(999L)).thenReturn(Mono.error(RuntimeException("Task not found")))

        webClient.get().uri("/api/tasks/999")
            .exchange()
            .expectStatus().isNotFound
    }
}