# FSocket
<h1>Intergration process</h1>

Because I'm not put this in any library repository, so to intergrate this library into your project following bellow step:<br>
- First, you should pull this repository to your local disk.
- Then open terminal, where gradlew.bat of this project locate.
- Type: gradlew buildLib. And it will automatically gennerate a folder in your desktop name FSocketLib(version)
- Then put the fsocket-release.aar file into your implementation project and confit it into your project by adding to build.gradle file:

```gradle
implementation project(":fsocket")
```
Also, copy the aar file into your project also.

<h1>FSocket Instruction</h1>

<h2>Initialization</h2>
For basic purpose, FSocket provides a very easy implementation.<br>
Create FSocket object using FSocketBuilder<br>

```kotlin
val fSocket = FSocketBuilder("wss://echo.websocket.org")
            .setConnectionTimeout(30_000L)
            .setReadTimeout(30_000L)
            .setPingInterval(2_000L)
            .build()
fSocket.connect()
```

<h2>Create listener</h2>

```kotlin
fSocket.run {
            addEventListener(OnConnectingListener {})
            addEventListener(OnConnectedListener {})
            addEventListener(OnConnectErrorListener {})
            addEventListener(OnMessageListener {})
            addEventListener(OnClosingListener {})
            addEventListener(OnClosedListener {})
            addEventListener(OnReconnectingListener {})
        }
```

- OnConnectingListener: Socket establishing
- OnConnectedListener: Socket connected
- OnConnectErrorListener: Socket connect error
- OnMessageListener: Socket receive a message
- OnClosingListener: Socket closing
- OnClosedListener: Socket closed, don't listen to channel signal
- OnReconnectingListener: Socket reconnecting

```kotlin
fSocket.terminate()
```

Terminate socket connection.
<h2>Specific use case</h2>

**If I'm not the one that told you to read this part, don't read, because you will be confused. And you do not need to read this.**<br>
But if you still read, you can go to FSocketHelper to see what I've done. Maybe you can learn something from Reflection in Kotlin..., maybe not<br>

<p>
This part is for specific use case which is send and receive data with BaseSocketData<br>
We provice FSocketHelper, and it uses some annotation technique to create socket service, and we should use this service to communicate with socket channel.<br>
For more undestanding, we will start with your service and what it should look like<br>
<br>

```kotlin
interface DemoService {
    @SendEvent(1)
    fun sendData(data: DataPush)

    @ReceiveEvent(1)
    suspend fun receiveData(): Flow<DataResponse>
}
```

Wondering what's inside DataPush and DataResponse?
I don't care if you do not wondering. But you should see this for some reason

```kotlin
data class DataPush(
    val customdata1: String
): BaseSocketData()

data class DataResponse(
    val customdata2: String
) : BaseSocketData()
```

It's bolth extend from 1 class call BaseSocketData.
And once again, let's see what inside
```kotlin
open class BaseSocketData(
    @SerializedName("type")
    var type: String = "",
    @SerializedName("departure")
    val departure: String = "1"
)
```
Because FSocketHelper is only parse data into data object that extends BaseSocketData. So all of you socket data either push or receive, need to be extend this class.<br>
Why I do that? Because it's a specific use case. And for me, it is my job.<br>

One note for you who use this part is because this receive is a Flow, so you need to put the listener on a coroutine to make sure it works.<br>

```kotlin
CoroutineScope(Dispatchers.IO).launch {
            service.receiveData().collect {
                //Anything can make you happy here
            }
        }
```

</p>
