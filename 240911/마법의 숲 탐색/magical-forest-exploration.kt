import java.io.*
import java.util.*

val north = 0
val east = 1
val south = 2
val west = 3

val dx = listOf(0, 0, 1, -1)
val dy = listOf(1, -1, 0, 0)
val rightIdx = 0
val leftIdx = 1
val downIdx = 2
val upIdx = 3
val checkMoveList = listOf(downIdx, leftIdx, rightIdx)

val field = -1

fun main(args: Array<String>) = with(Scanner(System.`in`)) {
    val br = BufferedReader(InputStreamReader(System.`in`))
    val bw = BufferedWriter(OutputStreamWriter(System.out))

    val info = br.readLine().split(" ").map {
        it.toInt()
    }
    val r = info[0] + 3
    val c = info[1]
    val k = info[2]
    var answer: Int = 0
    var board = createNewBoard(r, c)
    var exitBoard = createNewExitBoard(r, c)

    repeat(k) { id ->
        val data = br.readLine().split(" ").map {
            it.toInt()
        }

        var golem = Golem.initGolem(data[0] - 1)
        var exit = data[1]
        var visited = Array(r) {
            BooleanArray(c)
        }

        while (golem.bottom.second < r - 1) {

            var isMove = false

            if (!visited[golem.center.first][golem.center.second]) {
                visited[golem.center.first][golem.center.second] = true

                for (i in checkMoveList.indices) {
                    val moveGolem = golem.move(Pair(dx[checkMoveList[i]], dy[checkMoveList[i]]))
                    val validate = validateGolem(moveGolem, board)

                    if (validate) {
                        if (i == 1) {
                            exit = (exit + 3) % 4
                        } else if (i == 2) {
                            exit = (exit + 1) % 4
                        }

                        golem = moveGolem
                        isMove = true
                        break;
                    }
                }
            }

            if (golem.bottom.first == r - 1) {
                markBoards(id, exit, golem, board, exitBoard)

                val result = dfs(golem.center, board, exitBoard)

                answer += result - 3 + 1

                break
            }

            if (!isMove) {
                if (golem.isEnter()) {
                    markBoards(id, exit, golem, board, exitBoard)

                    val result = dfs(golem.center, board, exitBoard)

                    answer += result - 3 + 1
                } else {
                    board = createNewBoard(r, c)
                    exitBoard = createNewExitBoard(r, c)
                }

                break
            }
        }
    }

    bw.write(answer.toString())

    br.close()
    bw.close()
}

fun markBoards(id: Int, exitDir: Int, golem: Golem, board: Array<IntArray>, exitBoard: Array<BooleanArray>) {
    board[golem.top.first][golem.top.second] = id
    board[golem.bottom.first][golem.bottom.second] = id
    board[golem.left.first][golem.left.second] = id
    board[golem.right.first][golem.right.second] = id
    board[golem.center.first][golem.center.second] = id

    when (exitDir) {
        north -> {
            exitBoard[golem.top.first][golem.top.second] = true
        }
        south -> {
            exitBoard[golem.bottom.first][golem.bottom.second] = true
        }
        east -> {
            exitBoard[golem.right.first][golem.right.second] = true
        }
        else -> {
            exitBoard[golem.left.first][golem.left.second] = true
        }
    }
}

fun dfs(start: Pair<Int, Int>, board: Array<IntArray>, exitBoard: Array<BooleanArray>): Int {
    val visited = Array(board.size) {
        BooleanArray(board[0].size)
    }
    var result = start.first
    val q: Queue<Pair<Int, Int>> = LinkedList()
    q.offer(start)
    visited[start.first][start.second] = true

    while (q.isNotEmpty()) {
        val point = q.poll()

        for (i in dx.indices) {
            val nx = point.first + dx[i]
            val ny = point.second + dy[i]
            val canGoNextPoint = isInBoard(Pair(nx, ny), board) &&
                    board[nx][ny] != field &&
                    !visited[nx][ny] &&
                    (
                            board[nx][ny] == board[point.first][point.second] ||
                            exitBoard[point.first][point.second]
                            )

            if (canGoNextPoint) {
                visited[nx][ny] = true
                result = Integer.max(result, nx)

                q.offer(Pair(nx, ny))
            }
        }
    }

    return result
}

fun createNewBoard(r: Int, c: Int) = Array(r) {
    IntArray(c) {
        field
    }
}

fun createNewExitBoard(r: Int, c: Int) = Array(r) {
    BooleanArray(c)
}

// 각 포인트가 이동하는 방향에 따라
fun changePoint(cur: Pair<Int, Int>, dir: Pair<Int, Int>): Pair<Int, Int> {
    return Pair(
        cur.first + dir.first,
        cur.second + dir.second,
    )
}

data class Golem(
    val top: Pair<Int, Int>,
    val bottom: Pair<Int, Int>,
    val left: Pair<Int, Int>,
    val right: Pair<Int, Int>,
    val center: Pair<Int, Int>
) {
    companion object {
        fun initGolem(centerCol: Int): Golem {
            val centerPoint = Pair(1, centerCol)

            return Golem(
                changePoint(centerPoint, Pair(dx[upIdx], dy[upIdx])),
                changePoint(centerPoint, Pair(dx[downIdx], dy[downIdx])),
                changePoint(centerPoint, Pair(dx[leftIdx], dy[leftIdx])),
                changePoint(centerPoint, Pair(dx[rightIdx], dy[rightIdx])),
                centerPoint
            )
        }
    }

    // 골렘이 이동한다는 가정 하에 이동 완료된 골렘 객체
    fun move(dir: Pair<Int, Int>): Golem {
        return Golem(
            changePoint(top, dir),
            changePoint(bottom, dir),
            changePoint(left, dir),
            changePoint(right, dir),
            changePoint(center, dir)
        )
    }

    fun isEnter(): Boolean {
        return top.first > 2 && bottom.first > 2 && left.first > 2 && right.first > 2 && center.first > 2
    }
}

// 골렘이 위치할 수 있는 유효한 좌표인지
fun validateGolem(golem: Golem, board: Array<IntArray>): Boolean {
    return validatePoint(golem.top, board) &&validatePoint(golem.bottom, board) && validatePoint(golem.left, board) && validatePoint(golem.right, board) && validatePoint(golem.center, board)
}

// 해당 좌표가 유효한지 확인
fun validatePoint(point: Pair<Int,Int>, board: Array<IntArray>): Boolean {
    return isInBoard(point, board) && board[point.first][point.second] == field
}

// 해당 좌표가 보드판 안에 있는지
fun isInBoard(point: Pair<Int, Int>, board:Array<IntArray>) : Boolean {
    return point.first in 0 until board.size && point.second in 0 until board[0].size
}