import java.util.*

fun main(args: Array<String>) = with(Scanner(System.`in`)) {
    var starting = 0
    val tourMap = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
    // key : 여행상품 id, value : first - 가격 second - 목적지
    val tourItemMap = mutableMapOf<Int, Pair<Int, Int>>()
    val tourItemIdSet = mutableSetOf<Int>()
    // 랜드 집합
    val nodeSet = mutableSetOf<Int>()
    val answer = mutableListOf<Int>()
    val memoization = mutableMapOf<Pair<Int, Int>, Int>()

    // 출발지 변경
    fun changeStartingPoint(point: Int) {
        starting = point
        memoization.clear()
    }

    val orders = nextLine().toInt()

    repeat(orders) {
        val order = nextLine().split(" ")

        when (order[0].toInt()) {
            100 -> {
                for (i in 3 .. (order.size - 3) step 3) {
                    build(listOf(order[i].toInt(), order[i + 1].toInt()), order[i + 2].toInt(), tourMap, nodeSet)
                }
            }
            200 -> {
                createItem(order[1].toInt(), order[2].toInt(), order[3].toInt(), tourItemMap, tourItemIdSet)
            }
            300 -> {
                deleteItem(order[1].toInt(), tourItemIdSet)
            }
            400 -> {
                val findId = findBestItem(starting, tourMap, tourItemMap, tourItemIdSet, memoization)
                answer.add(findId?: -1)
                if (findId != null) {
                    tourItemIdSet.remove(findId)
                }
            }
            500 -> {
                changeStartingPoint(order[1].toInt())
            }
        }
    }

    println(answer.joinToString("\n"))
}

// 이동 가능한 노드와 가중치를 저장한 map을 만든다.
// 건설 명령
fun build(point: List<Int>, weight: Int, tourMap: MutableMap<Int, MutableList<Pair<Int, Int>>>, nodeSet: MutableSet<Int>) {
    if(tourMap[point[0]] == null) tourMap[point[0]] = mutableListOf()
    if(tourMap[point[1]] == null) tourMap[point[1]] = mutableListOf()

    tourMap[point[0]]?.add(Pair(point[1], weight))
    tourMap[point[1]]?.add(Pair(point[0], weight))

    nodeSet.add(point[0])
    nodeSet.add(point[1])
}

// 여행 상품 생성
// 투어 정보와 아이디를 모아놓는 집합을 이용해 관리
fun createItem(id: Int, price: Int, endPoint: Int, tourItemMap: MutableMap<Int, Pair<Int, Int>>, tourItemSet: MutableSet<Int>) {
    tourItemMap[id] = Pair(price, endPoint)
    tourItemSet.add(id)
}

// 여행 상품 취소
// 아이디를 모아놓는 집합에서 제거
fun deleteItem(id: Int, tourItemSet: MutableSet<Int>) {
    tourItemSet.remove(id)
}

fun findBestItem(starting: Int, tourMap: Map<Int, List<Pair<Int, Int>>>, tourItemMap: Map<Int, Pair<Int, Int>>, tourItemIdSet: Set<Int>, memoization: MutableMap<Pair<Int, Int>, Int>): Int? {
    var maxEarning: Int? = null
    var findId: Int?= null
    tourItemIdSet.forEach { id ->
        tourItemMap[id]?.let { tour ->
            val price = tour.first
            val endPoint = tour.second
            val nodeWeight = mutableMapOf<Int, Int>()
            // 노드 간 중복 방문을 막기 위함
            val visited = mutableMapOf<Pair<Int, Int>, Boolean>()
            // 최단거리를 구하기 위한 우선순위 큐
            val priorityQueue = PriorityQueue<Triple<Int, Int, Int>> { a, b ->
                a.third - b.third
            }
            nodeWeight[starting] = 0

            if (starting == endPoint) {
                if ((maxEarning?: -1) < price) {
                    maxEarning = price
                    findId = id
                } else if ((maxEarning?: -1) == price && id < (findId ?: -1)) {
                    findId = id
                }
            } else if (memoization[Pair(starting, endPoint)] != null) {
                val totalEarning = price - memoization[Pair(starting, endPoint)]!!

                if (totalEarning >= 0) {
                    if (maxEarning == null || totalEarning > maxEarning!!) {
                        maxEarning = totalEarning
                        findId = id
                    } else if (totalEarning == maxEarning) {
                        findId = if (Integer.min(id, findId?: -1) == -1) null else Integer.min(id, findId?: -1)
                    }
                }
            } else {
                val startingNodeList = tourMap[starting]?: listOf()

                startingNodeList.forEach {
                    priorityQueue.offer(Triple(starting, it.first, (nodeWeight[starting]?: Int.MAX_VALUE) + it.second))
                }

                while (priorityQueue.isNotEmpty()) {
                    val moveInfo = priorityQueue.poll()
                    val startPoint = moveInfo.first
                    val arrivePoint = moveInfo.second
                    val totalWeight = moveInfo.third
                    val nodes = mutableListOf(startPoint, arrivePoint)
                    nodes.sort()

                    if (visited[Pair(nodes[0], nodes[1])] == true) continue

                    visited[Pair(nodes[0], nodes[1])] = true
                    val minWeight = Integer.min(nodeWeight[arrivePoint]?: Int.MAX_VALUE, totalWeight)
                    nodeWeight[arrivePoint] = minWeight

                    if (arrivePoint == endPoint) {
                        memoization[Pair(starting, endPoint)] = nodeWeight[arrivePoint]?: Int.MAX_VALUE
                        val totalEarning = price - (nodeWeight[arrivePoint]?: Int.MAX_VALUE)

                        if (totalEarning >= 0) {
                            if (maxEarning == null || totalEarning > maxEarning!!) {
                                maxEarning = totalEarning
                                findId = id
                            } else if (totalEarning == maxEarning) {
                                findId = if (Integer.min(id, findId?: -1) == -1) null else Integer.min(id, findId?: -1)
                            }
                        }
                        break
                    }

                    if (maxEarning == null || (price - (nodeWeight[arrivePoint]?: Int.MAX_VALUE) >= (maxEarning?: Int.MAX_VALUE) )) {
                        val nextNodes = tourMap[arrivePoint] ?: listOf()
                        nextNodes.forEach {
                            priorityQueue.offer(Triple(arrivePoint, it.first, (nodeWeight[arrivePoint]?: Int.MAX_VALUE) + it.second))
                        }
                    }
                }
            }
        }
    }

    return findId
}