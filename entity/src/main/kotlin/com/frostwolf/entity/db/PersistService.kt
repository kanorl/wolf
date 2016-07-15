package com.frostwolf.entity.db

import com.frostwolf.common.concurrent.ExecutorContext
import com.frostwolf.common.concurrent.TaskPool
import com.frostwolf.entity.Entity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

internal interface PersistService {

    fun save(entity: Entity<*>, callback: (() -> Unit)? = null)

    fun update(entity: Entity<*>, callback: (() -> Unit)? = null)

    fun remove(entity: Entity<*>, callback: (() -> Unit)? = null)
}

@Component
internal open class PersistServiceImpl : PersistService {

    @Autowired
    private lateinit var persistence: Persistence

    private lateinit var taskPool: TaskPool

    @PostConstruct
    private fun init() {
        taskPool = ExecutorContext.createTaskPool("persist")
    }

    override fun save(entity: Entity<*>, callback: (() -> Unit)?) {
        submit(PersistTask.saveTask(persistence, entity, callback))
    }

    override fun update(entity: Entity<*>, callback: (() -> Unit)?) {
        submit(PersistTask.updateTask(persistence, entity, callback))
    }

    override fun remove(entity: Entity<*>, callback: (() -> Unit)?) {
        submit(PersistTask.removeTask(persistence, entity, callback))
    }

    internal fun submit(task: PersistTask) {
        taskPool.submit(task.entity.id, task)
    }
}

//
//@Component
//internal class ScheduledPersistService : PersistService {
//    private val logger by getLogger()
//
//    @Autowired
//    private lateinit var persistence: Persistence
//    @Autowired
//    private lateinit var delegate: ImmediatePersistService
//    @Autowired
//    private lateinit var setting: EntitySetting
//    @Autowired
//    private lateinit var scheduler: Scheduler
//
//    private val cache = ConcurrentHashMap<Class<*>, ConcurrentHashMap<Any, PersistTask>>()
//    private val lock = ReentrantReadWriteLock()
//    private val r = lock.readLock()
//    private val w = lock.writeLock()
//
//    @PostConstruct
//    private fun init() {
//        scheduler.scheduleWithFixedDelay(setting.persistInterval.seconds(), "scheduled-persist-task") { persist() }
//    }
//
//    @PreDestroy
//    private fun onDestroy() {
//        persist()
//    }
//
//    override fun save(entity: IEntity<*>, callback: (() -> Unit)?) {
//        val map = mapFor(entity)
//        val task = PersistTask.saveTask(persistence, entity, callback)
//        r.lock { map.put(entity.id, task) }
//    }
//
//    override fun update(entity: IEntity<*>, callback: (() -> Unit)?) {
//        val map = mapFor(entity)
//        val task = PersistTask.saveTask(persistence, entity, callback)
//        r.lock { map.putIfAbsent(entity.id, task) }
//    }
//
//    override fun remove(entity: IEntity<*>, callback: (() -> Unit)?) {
//        val map = mapFor(entity)
//        val task = PersistTask.saveTask(persistence, entity, callback)
//        r.lock { map.put(entity.id, task) }
//    }
//
//    private fun mapFor(entity: IEntity<*>): ConcurrentHashMap<Any, PersistTask> = cache[entity.javaClass] ?: cache.computeIfAbsent(entity.javaClass, { ConcurrentHashMap() })
//
//    private fun persist() {
//        var tasks = emptyList<PersistTask>()
//        w.lock {
//            tasks = cache.values.flatMap { it.values }.toList()
//            cache.values.clear()
//        }
//        tasks.forEach { delegate.submit(it) }
//
//        logger.debug("{} PersistTask submitted.", tasks.size)
//    }
//}
