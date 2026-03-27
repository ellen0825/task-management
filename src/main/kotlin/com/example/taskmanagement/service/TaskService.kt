@SpringBootTest
class TaskServiceTest @Autowired constructor(
    val taskService: TaskService
) {

    @Test
    fun `create task`() {
        val request = TaskRequest(title = "Unit Test", description = "Test Desc")
        val created = taskService.createTask(request).block()!!
        assertEquals("Unit Test", created.title)
        assertEquals(TaskStatus.NEW, created.status)
    }

    @Test
    fun `get task by id - not found`() {
        val result = taskService.getTaskById(9999L)
        StepVerifier.create(result)
            .expectErrorMatches { it is RuntimeException && it.message == "Task not found" }
            .verify()
    }

    @Test
    fun `update task status`() {
        val request = TaskRequest("Update Status", "Desc")
        val task = taskService.createTask(request).block()!!
        val updated = taskService.updateStatus(task.id!!, TaskStatus.DONE).block()!!
        assertEquals(TaskStatus.DONE, updated.status)
    }
}