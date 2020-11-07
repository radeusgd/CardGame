package com.radeusgd.trachonline

import com.radeusgd.trachonline.messages.ClientMessage
import com.radeusgd.trachonline.messages.Exited
import com.radeusgd.trachonline.messages.Joined
import com.radeusgd.trachonline.messages.ServerMessage
import java.util.*

abstract class Server<ClientData> {
    private val clients = java.util.concurrent.ConcurrentHashMap<UUID, Pair<Client, ClientData>>()
    fun broadcast(message: ServerMessage) =
        clients.forEach { _, (client, _) ->
            client.sendMessage(message)
        }

    fun connectedClients(): List<Client> = clients.map { it.value.first }

    fun onJoined(client: Client) = client.run {
        initializeClientData(this).let { clientData -> clients.put(uuid(), Pair(this, clientData)) }
        onMessage(this, Joined)
    }

    fun onExited(client: Client) = client.run {
        onMessage(this, Exited)
        clients.remove(uuid())
    }

    private val lock = object {}
    fun onMessage(client: Client, message: ClientMessage) {
        synchronized(lock) {
            handleMessage(client, message)
        }
    }

    fun getClientData(client: Client) =
        getClientData(client.uuid()) ?: throw IllegalStateException("A client was passed that is no longer connected")

    fun getClientData(uuid: UUID): ClientData? = clients[uuid]?.second

    abstract fun initializeClientData(client: Client): ClientData

    /** Handle message calls are synchronized - they will be called one after another,
     * but they should not do any blocking operations. */
    abstract fun handleMessage(client: Client, message: ClientMessage)
}
