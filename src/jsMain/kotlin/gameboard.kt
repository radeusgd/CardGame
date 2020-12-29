import com.radeusgd.trachonline.board.GameSnapshot
import kotlinx.css.Color
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.p
import styled.css
import styled.styledDiv

data class GameboardProps(var gameSnapshot: GameSnapshot) : RProps

interface GameboardState : RState

@JsExport
class Gameboard(props: GameboardProps) : RComponent<GameboardProps, GameboardState>(props) {

    val playerPersonalAreaScale = 3f

    override fun RBuilder.render() {
        styledDiv {
            css {
                +GameStyles.mainAreaContainer
            }
            child(BoardAreaView::class) {
                attrs {
                    board = props.gameSnapshot.mainArea
                    entityBaseSize = 5f
                    name = "Główna plansza"
                    background = GameStyles.boardAreaColor
                    includeStackMaker = true
                }
            }
        }
        styledDiv {
            css {
                +GameStyles.playersContainer
            }
            +"Gracze"

            props.gameSnapshot.players.forEach {
                p {
                    +"${it.name}: ${it.privateAreaCount} kart na ręce"
                }

                styledDiv {
                    css {
                        +GameStyles.personalArea(playerPersonalAreaScale)
                    }
                    child(BoardAreaView::class) {
                        attrs {
                            board = it.personalArea
                            entityBaseSize = playerPersonalAreaScale
                            name = null
                            background = GameStyles.boardAreaColor
                            includeStackMaker = false
                        }
                    }
                }
            }
        }
        styledDiv {
            css {
                +GameStyles.privateAreaContainer
            }

            child(BoardAreaView::class) {
                attrs {
                    board = props.gameSnapshot.privateArea
                    entityBaseSize = 7f
                    name = "Karty na ręce"
                    background = Color("#938521")
                    includeStackMaker = false
                }
            }
        }
    }

    companion object {
        val BoardAreaClass = "board-area"
        val StackMakerClass = "stack-maker"
    }
}
