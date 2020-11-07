import com.benasher44.uuid.Uuid
import com.radeusgd.trachonline.board.BoardDestination
import com.radeusgd.trachonline.board.Card
import com.radeusgd.trachonline.board.CardStack
import com.radeusgd.trachonline.board.PlacedEntity
import com.radeusgd.trachonline.board.Position
import com.radeusgd.trachonline.messages.FlipCard
import com.radeusgd.trachonline.messages.MoveEntity
import com.radeusgd.trachonline.messages.PickStack
import kotlin.math.max
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.IMG
import kotlinx.html.js.onDoubleClickFunction
import kotlinx.html.js.onDragEndFunction
import org.w3c.dom.DragEvent
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.br
import styled.css
import styled.styledDiv
import styled.styledImg

// TODO parent board id may be obsolete if we are able to detect boards by position, but we still need some notion of what boards are available
data class EntityProps(var placedEntity: PlacedEntity, var baseSize: Float, var parentBoardId: Uuid) : RProps

interface EntityState : RState

@JsExport
class EntityView(props: EntityProps) : RComponent<EntityProps, EntityState>(props) {

    override fun RBuilder.render() {
        styledDiv {
            css {
                +GameStyles.placedEntityCss(props.placedEntity)
            }
            val entity = props.placedEntity.entity

            val setupDragging: IMG.() -> Unit = {
                onDragEndFunction = { event ->
                    console.log(event)
                    val dragged = (event.asDynamic().nativeEvent as? DragEvent)
                    dragged?.let {
                        // TODO checking for stack
                        val vw = max(document.documentElement?.clientWidth ?: 0, window.innerWidth)
                        if (vw == 0) {
                            console.error("Could not estimate viewport width!")
                        } else {
                            val x: Float = (it.pageX / vw * 100f).toFloat()
                            val y: Float = (it.pageY / vw * 100f).toFloat()
                            console.log(x to y)
                            // TODO possibility to move to different board
                            // TODO how to detect board id based on position???
                            val board = props.parentBoardId
                            val depth = 0 // TODO depth
                            val destination = BoardDestination(board, Position(x, y, depth))
                            sendMessage(MoveEntity(entity.uuid, destination))
                        }
                    }
                }
            }

            when (entity) {
                is CardStack ->
                    styledDiv {
                        css {
                            +GameStyles.cardStackBorderStyle
                        }
                        +"(${entity.cards.size} cards)"
                        br { }
                        styledImg(src = "/static/" + (entity.cards.firstOrNull()?.getCurrentImage() ?: "error.jpg")) {
                            css {
                                +GameStyles.cardStyle(props.baseSize)
                            }
                            attrs {
                                setupDragging()
                                onDoubleClickFunction = { event ->
                                    sendMessage(PickStack(entity.uuid))
                                }
                            }
                        }
                    }
                is Card ->
                    styledImg(src = "/static/" + entity.getCurrentImage()) {
                        css {
                            +GameStyles.cardStyle(props.baseSize)
                        }
                        attrs {
                            setupDragging()
                            onDoubleClickFunction = { event ->
                                sendMessage(FlipCard(entity.uuid))
                            }
                        }
                    }
            }
        }
    }
}
