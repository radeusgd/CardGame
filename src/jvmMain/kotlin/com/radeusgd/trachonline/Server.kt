package com.radeusgd.trachonline

import messages.Exited
import messages.Joined
import messages.Message
import java.util.*

abstract class Server<ClientData> {
    private val clients = java.util.concurrent.ConcurrentHashMap<UUID, Pair<Client, ClientData>>()
    fun broadcast(message: Message) =
        clients.forEach { _, (client, _) ->
            client.sendMessage(message)
        }

     fun onJoined(client: Client) = client.run {
         onMessage(this, Joined)
         initializeClientData(this).let { clientData -> clients.put(uuid(), Pair(this, clientData)) }
     }

    fun onExited(client: Client) = client.run {
        onMessage(this, Exited)
        clients.remove(uuid())
    }

    fun getClientData(client: Client) =
        getClientData(client.uuid()) ?: throw IllegalStateException("A client was passed that is no longer connected")

    private fun getClientData(uuid: UUID): ClientData? = clients[uuid]?.second

    abstract fun initializeClientData(client: Client): ClientData
    abstract fun onMessage(client: Client, message: Message)
}
