import java.util.*

val turningDegrees = listOf(90, 180, 270)
val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))

fun main(args: Array<String>) = with(Scanner(System.`in`)) {
    val answer = mutableListOf<Int>()
    val searchInfo = nextLine().split(" ")
    val maxTurn = searchInfo[0].toInt()
    val exItemsCnt = searchInfo[1].toInt()
    // 유물지도
    val itemMap = Array(5) {
        IntArray(5) {
            -1
        }
    }

    for (i in 0 until 5) {
        val itemList = nextLine().split(" ")
        for (j in itemList.indices) {
            itemMap[i][j] = itemList[j].toInt()
        }
    }
    var readyToUseExItemIdx = 0
    val exItemList = nextLine().split(" ").map {
        it.toInt()
    }

    for (i in 1 .. maxTurn) {
        var total = 0
        val selectedCore = searchCore(itemMap)
        if (selectedCore.first == null && selectedCore.second == null) break

        turn(selectedCore.first!!, itemMap, selectedCore.second!!)

        while (true) {
            val changeList = findAll(itemMap)

            if (changeList.isEmpty()) break
            changeList.forEach {
                itemMap[it.first][it.second] = exItemList[readyToUseExItemIdx]
                readyToUseExItemIdx++
            }
            total += changeList.size
        }

        answer.add(total)
    }

    println(answer.joinToString(" "))
}

// 현재 맵에서 조건에 최적화된 점을 찾는 함수
fun searchCore(currentMap: Array<IntArray>): Pair<Pair<Int, Int>?, Int?> {
    var selectedCore: Pair<Int, Int>? = null
    var maxScore = 0
    var selectedDegree: Int? = null

    fun changeResult(core: Pair<Int, Int>, score: Int, degree: Int) {
        selectedCore = core
        maxScore = score
        selectedDegree = degree
    }
    for (i in 1..3) {
        for (j in 1..3) {
            for (degree in turningDegrees) {
                val core = Pair(i, j)
                val temp = Array(5) {
                    IntArray(5) {
                        -1
                    }
                }
                for (col in 0..4) {
                    for (row in 0..4) {
                        temp[col][row] = currentMap[col][row]
                    }
                }
                turn(core, temp, degree)
                val findResult = findAll(temp)
                val score = findResult.size
                if (score > maxScore) {
                    changeResult(core, score, degree)
                    selectedCore = core
                    maxScore = score
                    selectedDegree = degree
                } else if (score == maxScore && maxScore != 0 && selectedCore != null) {
                    if (degree < selectedDegree!!) {
                        changeResult(core, score, degree)
                    } else if (degree == selectedDegree!!) {
                        if (core.second < selectedCore!!.second) {
                            changeResult(core, score, degree)
                        } else if (core.second == selectedCore!!.second) {
                            if (core.first < selectedCore!!.first) {
                                changeResult(core, score, degree)
                            }
                        }
                    }
                }
            }
        }
    }
    return Pair(selectedCore, selectedDegree)
}

// 해당 지점으로 돌렸을 때 점수 합, 지도 변형
fun turn(core: Pair<Int, Int>, itemMap: Array<IntArray>, degree: Int) {
    val arr = IntArray(8)
    val startIdx = 8 - 2 * (degree / 90)
    arr[0] = itemMap[core.first - 1][core.second - 1]
    arr[1] = itemMap[core.first - 1][core.second]
    arr[2] = itemMap[core.first - 1][core.second + 1]
    arr[3] = itemMap[core.first][core.second + 1]
    arr[4] = itemMap[core.first + 1][core.second + 1]
    arr[5] = itemMap[core.first + 1][core.second]
    arr[6] = itemMap[core.first + 1][core.second - 1]
    arr[7] = itemMap[core.first][core.second - 1]

    for (i in arr.indices) {
        val cur = if (startIdx + i >= arr.size) {
            startIdx + i - arr.size
        } else {
            startIdx + i
        }
        when (i) {
            0 -> {
                itemMap[core.first - 1][core.second - 1] = arr[cur]
            }
            1 -> {
                itemMap[core.first - 1][core.second] = arr[cur]
            }
            2 -> {
                itemMap[core.first - 1][core.second + 1] = arr[cur]
            }
            3 -> {
                itemMap[core.first][core.second + 1] = arr[cur]
            }
            4 -> {
                itemMap[core.first + 1][core.second + 1] = arr[cur]
            }
            5 -> {
                itemMap[core.first + 1][core.second] = arr[cur]
            }
            6 -> {
                itemMap[core.first + 1][core.second - 1] = arr[cur]
            }
            7 -> {
                itemMap[core.first][core.second - 1] = arr[cur]
            }
        }
    }
}

fun findAll(turnedItemMap: Array<IntArray>): List<Pair<Int, Int>> {
    val visited = mutableMapOf<Pair<Int, Int>, Boolean>()
    val total = mutableSetOf<Pair<Int, Int>>()
    val scoreList = mutableListOf<Int>()
    val changeArr = Array(5) {
        IntArray(5) {
            -1
        }
    }

    for (i in 0 until 5) {
        for (j in 0 until 5) {
            if (visited[Pair(i, j)] == true) continue

            val queue: Queue<Pair<Int, Int>> = LinkedList()
            val point = turnedItemMap[i][j]
            val pointSet = mutableSetOf<Pair<Int, Int>>()
            queue.offer(Pair(i, j))

            while(queue.isNotEmpty()) {
                val searchPoint = queue.poll()
                pointSet.add(searchPoint)
                visited[searchPoint] = true

                for (dir in dirs) {
                    val nextPoint = Pair(searchPoint.first + dir.first, searchPoint.second + dir.second)
                    if (nextPoint.first in 0..4 && nextPoint.second in 0..4 && visited[nextPoint] != true && turnedItemMap[nextPoint.first][nextPoint.second] == point) {
                        queue.offer(nextPoint)
                    }
                }
            }

            if (pointSet.size >= 3) {
                total.addAll(pointSet)
                scoreList.add(point)
            }
        }
    }
    val priorityQueue = PriorityQueue<Pair<Int, Int>> { prev, next ->
        when {
            prev.second < next.second -> {
                1
            }
            prev.second == next.second -> {
                when {
                    prev.first > next.first -> 1
                    prev.first < next.first -> -1
                    else -> 0
                }
            }
            else -> {
                -1
            }
        }
    }
    total.forEach {
        changeArr[it.first][it.second] = 1
    }
    val result = mutableListOf<Pair<Int, Int>>()

    for (i in 0 until 5) {
        for (j in 0 until 5) {
            if (changeArr[4-j][i] == 1) {
                result.add(Pair(4-j, i))
            }
        }
    }

    while (priorityQueue.isNotEmpty()) {
        val point = priorityQueue.poll()
        result.add(point)
    }

    return result
}