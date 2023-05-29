package ru.geekbrains.lesson2;

import java.util.*;

public class Program {
    private static final int WIN_COUNT = 6; // Условие выигрыша
    private static final int fieldSizeX = 10; // Размерность игрового поля
    private static final int fieldSizeY = 10; // Размерность игрового поля
    private static final char DOT_HUMAN = 'X';
    private static final char DOT_AI = 'O';
    private static final char DOT_EMPTY = '•';
    private static final Scanner SCANNER = new Scanner(System.in);
    private static char[][] field; // Двумерный массив хранит текущее состояние игрового поля
    private static boolean DRAW = false;


    public static void main(String[] args) {
        while (true) {
            initialize();
            printField();
            while (true) {
                humanTurn();
                if (gameCheck(DOT_HUMAN, "Вы победили!"))
                    break;
                aiTurn();
                printField();
                if (gameCheck(DOT_AI, "Компьютер победил!"))
                    break;
            }
            System.out.println("Желаете сыграть еще раз? (Y - да)");
            if (!SCANNER.next().equalsIgnoreCase("Y"))
                break;
        }
    }

    //Инициализация игрового поля
    private static void initialize() {
        field = new char[fieldSizeX][fieldSizeY];
        // Пройдем по всем элементам массива
        for (int x = 0; x < fieldSizeX; x++) {
            for (int y = 0; y < fieldSizeY; y++) {
                // Проинициализируем все элементы массива DOT_EMPTY (признак пустого поля)
                field[x][y] = DOT_EMPTY;
            }
        }
    }

    private static void printField() {
        System.out.print("+");
        for (int i = 0; i < fieldSizeX * 2 + 1; i++) {
            System.out.print((i % 2 == 0) ? "-" : i / 2 + 1);
        }
        System.out.println();

        for (int i = 0; i < fieldSizeY; i++) {
            System.out.print(i + 1 + "|");

            for (int j = 0; j < fieldSizeX; j++)
                System.out.print(field[j][i] + "|");

            System.out.println();
        }

        for (int i = 0; i < fieldSizeX * 2 + 2; i++) {
            System.out.print("-");
        }
        System.out.println();

    }

    /**
     * Обработка хода игрока (человек)
     */
    private static void humanTurn() {
        int x, y;
        do {
            System.out.print("Введите координаты хода X и Y (от 1 до " + WIN_COUNT + ") через пробел >>> ");
            x = SCANNER.nextInt() - 1;
            y = SCANNER.nextInt() - 1;
        }
        while (!isCellValid(x, y) || !isCellEmpty(x, y));
        field[x][y] = DOT_HUMAN;
    }

    //Проверка, ячейка является пустой
    static boolean isCellEmpty(int x, int y) {
        return field[x][y] == DOT_EMPTY;
    }

    /**
     * Проверка корректности ввода
     * (координаты хода не должны превышать размерность массива, игрового поля)
     *
     * @param x
     * @param y
     * @return
     */
    static boolean isCellValid(int x, int y) {
        return x >= 0 && x < fieldSizeX && y >= 0 && y < fieldSizeY;
    }

    /**
     * Ход компьютера.
     * Компьютер смотрит на свои возможные комбинации и на игрока.
     */
    private static void aiTurn() {
        Queue<Integer[]> aiStrategy = fieldCheck(DOT_AI);
        Queue<Integer[]> humanStrategy = fieldCheck(DOT_HUMAN);

        Integer[] integers = nextTurnCalc(aiStrategy, humanStrategy);
        if (integers != null) {
            field[integers[0]][integers[1]] = DOT_AI;
        }
    }

    // Проверка идёт по комбинациям в списке. Если хоть одна == WIN_COUNT, то победа зачитывается
    static boolean checkWin(char symbol) {
        for (List<Queue<Integer[]>> variant : getAllVariants(symbol)) {
            for (Queue<Integer[]> integers : variant) {
                int count = 0;
                if (integers != null) {
                    for (Integer[] integer : integers) {
                        if (integer[0] == 1) {
                            count++;
                        }
                    }
                }
                if (count == WIN_COUNT) {
                    return true;
                }
            }
        }
        return false;
    }

    static Queue<Integer[]> fieldCheck(char symbol) {
        return calcStrategy(getAllVariants(symbol));
    }

    //Список со всем вариантами ходов
    static List<List<Queue<Integer[]>>> getAllVariants(char symbol) {
        List<List<Queue<Integer[]>>> allVariants = new ArrayList<>();
        for (int x = 0; x < fieldSizeX; x++) {
            for (int y = 0; y < fieldSizeY; y++) {
                if (field[x][y] == symbol) {
                    allVariants.add(queueCheck(symbol, x, y));
                }
            }
        }
        return allVariants;
    }

    //Поиск наилучшей стратегии для хода.
    //Идея - найти ту стратегию, которая самая быстрая.
    static Queue<Integer[]> calcStrategy(List<List<Queue<Integer[]>>> allVariants) {
        int max = 0;
        //поиск наибольшего количества вариантов выигрыша
        for (List<Queue<Integer[]>> list : allVariants) {
            int size = list.size();
            if (size > max) {
                max = size;
            }
        }
        final int maxSize = max;
        //исключение менее вариативных списков
        List<List<Queue<Integer[]>>> shortListVariants = allVariants.stream().filter(list -> list.size() == maxSize).toList();
        //поиск самой быстрой стратегии по наименьшему количеству ходов (p=0)
        Queue<Integer[]> fatsStrategy = null;
        int steps = WIN_COUNT;
        for (List<Queue<Integer[]>> list : shortListVariants) {
            for (Queue<Integer[]> integers : list) {
                //проверяем количество шагов до выигрыша в каждой стратегии
                int currentSteps = 0;
                if (integers != null) {
                    for (Integer[] integer : integers) {
                        if (integer[0] == 0) {
                            currentSteps++;
                        }
                    }
                    //обновляем наименьшее количество ходов до выигрыша
                    if (currentSteps <= steps) {
                        steps = currentSteps;
                        fatsStrategy = integers;
                    }
                }
            }
        }
        return fatsStrategy;
    }

    /**
     * Определение хода с учётом возможности победы игрока.
     * Если у игрока больше шансов, то компьютер вредит игроку.
     * Иначе - играет свои комбинации.
     *
     * @param aiStrategy    стратегия компьютера.
     * @param humanStrategy стратегия игрока.
     * @return координаты следующего хода.
     */
    static Integer[] nextTurnCalc(Queue<Integer[]> aiStrategy, Queue<Integer[]> humanStrategy) {
        //Если нет возможных стратегий, то ничья - играть дальше бессмысленно.
        if (humanStrategy == null && aiStrategy == null) {
            DRAW = true;
        } else {
            int aiTurns;
            int humanTurns;
            if (aiStrategy != null) {
                if (humanStrategy == null) {
                    return choseTurnPoint(aiStrategy);
                } else {
                    aiTurns = turnsCount(aiStrategy);
                    humanTurns = turnsCount(humanStrategy);
                    if (aiTurns < humanTurns) {
                        return choseTurnPoint(aiStrategy);
                    } else return choseTurnPoint(humanStrategy);
                }
            } else if (humanStrategy != null) {
                return choseTurnPoint(humanStrategy);
            }
        }
        return null;
    }

    //Подсчёт ходов в стратегии.
    static int turnsCount(Queue<Integer[]> Strategy) {
        int result = 0;
        //Если стоит 1, значит туда уже ходили. Учитывать не надо.
        for (Integer[] integers : Strategy) {
            if (integers[0] == 0) {
                result++;
            }
        }
        return result;
    }

    //Следующий ход.
    static Integer[] choseTurnPoint(Queue<Integer[]> strategy) {
        strategy.poll();
        int flag = 0;
        for (Integer[] integers : strategy) {
            if (integers[0] == 1) {
                flag = 1;
                break;
            }
        }
        if (flag == 1) {
            Integer[] currentTurn = strategy.poll();
            while (currentTurn[0] == 1) {
                currentTurn = strategy.poll();
            }
            return new Integer[]{currentTurn[1], currentTurn[2]};
        } else {
            Integer[] nextTurn = strategy.poll();
            return new Integer[]{nextTurn[1], nextTurn[2]};
        }
    }

    /**
     * Создание списка вариантов ходов для конкретной точки.
     *
     * @param symbol символ для проверки
     * @param x      координата.
     * @param y      координата.
     * @return список стратегий для разыгрывания хода.
     */
    static List<Queue<Integer[]>> queueCheck(char symbol, int x, int y) {
        List<Queue<Integer[]>> variants = new ArrayList<>();
        variants.add(vertCheck(symbol, x, y));
        variants.add(horizCheck(symbol, x, y));
        variants.add(rightDiagCheck(symbol, x, y));
        variants.add(wrongDiagCheck(symbol, x, y));
        return variants;
    }

    //проверяем вертикаль
    static Queue<Integer[]> vertCheck(char symbol, int x, int y) {
        Queue<Integer[]> queue = new ArrayDeque<>();
        int j = y;
        while (pointCheck(queue, symbol, x, j) && queue.size() <= WIN_COUNT) {
            j--;
        }
        j = y + 1;
        while (pointCheck(queue, symbol, x, j) && queue.size() <= WIN_COUNT) {
            j++;
        }
        if (!(queue.size() < WIN_COUNT)) {
            return queue;
        }
        return null;
    }

    //проверяем горизонталь
    static Queue<Integer[]> horizCheck(char symbol, int x, int y) {
        Queue<Integer[]> queue = new ArrayDeque<>();
        int i = x;
        while (pointCheck(queue, symbol, i, y) && queue.size() <= WIN_COUNT) {
            i--;
        }
        i = x + 1;
        while (pointCheck(queue, symbol, i, y) && queue.size() <= WIN_COUNT) {
            i++;
        }
        if (!(queue.size() < WIN_COUNT)) {
            return queue;
        }
        return null;
    }

    //проверяем правильную диагональ
    static Queue<Integer[]> rightDiagCheck(char symbol, int x, int y) {
        Queue<Integer[]> queue = new ArrayDeque<>();
        int i = x;
        int j = y;
        while (pointCheck(queue, symbol, i, j) && queue.size() <= WIN_COUNT) {
            i--;
            j--;
        }
        i = x + 1;
        j = y + 1;
        while (pointCheck(queue, symbol, i, j) && queue.size() <= WIN_COUNT) {
            i++;
            j++;
        }
        if (!(queue.size() < WIN_COUNT)) {
            return queue;
        }
        return null;
    }

    //проверяем неправильную диагональ
    static Queue<Integer[]> wrongDiagCheck(char symbol, int x, int y) {
        Queue<Integer[]> queue = new ArrayDeque<>();
        int i = x;
        int j = y;
        while (pointCheck(queue, symbol, i, j) && queue.size() <= WIN_COUNT) {
            i++;
            j--;
        }
        i = x - 1;
        j = y + 1;
        while (pointCheck(queue, symbol, i, j) && queue.size() <= WIN_COUNT) {
            i--;
            j++;
        }
        if (!(queue.size() < WIN_COUNT)) {
            return queue;
        }
        return null;
    }

    /**
     * Если точка пустая или нужного нам символа, то добавляем её в очередь.
     * Если она пустая, то добавляется с индексом p=0. Означает то, что в неё можно сделать ход.
     *
     * @param queue  очередь для добавления позиции.
     * @param symbol символ для сравнения.
     * @param y      координата.
     * @param x      координата.
     * @return true, если точка добавлена, false, если добавить точку нельзя.
     */
    static boolean pointCheck(Queue<Integer[]> queue, char symbol, int x, int y) {
        int p;
        if (isCellValid(x, y)) {
            if (field[x][y] == symbol || field[x][y] == DOT_EMPTY) {
                p = 1;
                if (field[x][y] == DOT_EMPTY) {
                    p = 0;
                }
                queue.add(new Integer[]{p, x, y});
                return true;
            } else return false;
        } else return false;
    }

    //Проверка на ничью
    static boolean checkDraw() {
        return DRAW;
    }

    //Метод проверки состояния игры
    static boolean gameCheck(char c, String str) {
        if (checkWin(c)) {
            System.out.println(str);
            return true;
        }
        if (checkDraw()) {
            System.out.println("Ничья!");
            return true;
        }

        return false; // Игра продолжается
    }
}
