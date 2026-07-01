package mDimension.world.blocks.flux;

import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.IntSet;
import arc.struct.Queue;
import arc.struct.Seq;
import mDimension.consumers.modules.FluxModule;
import mDimension.tool.Debug;
import mDimension.world.MDEvents;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.graphics.Pal;

import static mDimension.world.blocks.flux.Fluxs.*;
import static mindustry.Vars.world;

public class FluxGraph {
    // ========== 静态常量 ==========
    private static final int INITIAL_CAPACITY = 16;

    // ========== 实例变量（每个图独立） ==========
    // BFS 缓冲区 - 实例变量避免多图竞争
    private final IntSet visited = new IntSet();
    private final Queue<Building> queue = new Queue<>();
    private final Seq<Building> outArray1 = new Seq<>(false, INITIAL_CAPACITY, Building.class);
    private final Seq<Building> outArray2 = new Seq<>(false, INITIAL_CAPACITY, Building.class);

    // 图状态
    public boolean deprecate = false;
    public boolean init = false;
    private int lastChange = -2;

    // 建筑列表 - 用 false 避免类型检查开销
    public final Seq<Building> all = new Seq<>(false, INITIAL_CAPACITY, Building.class);
    public final Seq<Building> storage = new Seq<>(false, INITIAL_CAPACITY, Building.class);
    public final Seq<Building> consumer = new Seq<>(false, INITIAL_CAPACITY, Building.class);

    // 调试用
    public String debugLog = "";

    // ========== 初始化 ==========
    public void init(Building b) {
        if (!init) {
            MDEvents.graphs.addUnique(this);
        }
        add(b);
        init = true;
    }

    public void init() {
        if (!init) {
            MDEvents.graphs.addUnique(this);
        }
        init = true;
    }

    // ========== 核心操作 ==========
    public void add(Building b) {
        if (!(b instanceof Flux f)) return;

        FluxModule flux = f.flux();
        var cons = f.consFlux();
        if (flux == null) return;

        all.add(b);

        if (cons.buffered && cons.capacity > 0) {
            storage.add(b);
        }
        if (cons.usage > 0) {
            consumer.add(b);
        }

        // 从旧图移除，指向新图
        if (flux.graph != this && flux.graph != null) {
            flux.graph.removeList(b);
        }
        flux.graph = this;
    }

    public void removeList(Building b) {
        all.remove(b);
        storage.remove(b);
        consumer.remove(b);
    }

    public void clear() {
        all.clear();
        storage.clear();
        consumer.clear();
    }

    public void deprecate() {
        clear();
        MDEvents.graphs.remove(this);
        deprecate = true;
    }

    // ========== BFS（对齐原版 PowerGraph） ==========
    /**
     * 标准 BFS，用于 updateGraph 重建
     */
    public void bfs(Building start) {
        clear();
        queue.clear();
        visited.clear();
        queue.addLast(start);
        add(start);
        visited.add(start.pos());

        while (!queue.isEmpty()) {
            Building node = queue.removeFirst();
            for (Building c : FluxConnections(node, outArray1)) {
                FluxModule cflux = flux(c);
                if (c != null && visited.add(c.pos()) && cflux != null) {
                    if (cflux.graph != this) {
                        cflux.graph.removeList(c);
                    }
                    add(c);
                    Fx.colorTrail.at(c.x, c.y, 2, Color.valueOf("4040ff"));
                    queue.addLast(c);
                }
            }
        }
        init();
    }

    /**
     * 带屏障的 BFS，用于 remove 时绕开被拆建筑
     */
    public void bfs(Building start, Building barrier, Cons<Building> cons) {
        clear();
        queue.clear();
        visited.clear();
        queue.addLast(start);
        visited.add(start.pos());

        add(start);

        while (!queue.isEmpty()) {
            Building node = queue.removeFirst();
            if (cons != null) cons.get(node);

            for (Building c : FluxConnections(node, outArray1)) {
                if (c == barrier) continue;
                FluxModule cflux = flux(c);
                if (c != null && visited.add(c.pos()) && cflux != null) {
                    if (cflux.graph != this) {
                        cflux.graph.removeList(c);
                    }
                    add(c);
                    Fx.colorTrail.at(c.x, c.y, 2, Color.valueOf("4040ff"));
                    queue.addLast(c);
                }
            }
        }
        init();
    }

    // ========== 数值计算 ==========
    public float getCapacity() {
        float totalCapacity = 0f;
        var items = storage.items;
        for (int i = 0; i < storage.size; i++) {
            var battery = items[i];
            if (battery instanceof Flux f) {
                totalCapacity += (f.consFlux().capacity - f.flux().fluxAmount);
            }
        }
        return totalCapacity;
    }

    public float getStorage() {
        float amount = 0f;
        var items = storage.items;
        for (int i = 0; i < storage.size; i++) {
            var battery = items[i];
            if (battery instanceof Flux f) {
                amount += f.consFlux().capacity;
            }
        }
        return amount;
    }

    public float getTotal() {
        float amount = 0f;
        var items = storage.items;
        for (int i = 0; i < storage.size; i++) {
            var battery = items[i];
            if (battery instanceof Flux f) {
                amount += f.flux().fluxAmount;
            }
        }
        return amount;
    }

    public boolean isOverload() {
        return getCapacity() < 0.1f;
    }

    public void changeCapacity(float excess) {
        float capacity = getCapacity();
        if (Mathf.equal(capacity, 0f)) return;

        float chargedPercent = Mathf.clamp(excess / capacity, -1, 1);
        var items = storage.items;
        for (int i = 0; i < storage.size; i++) {
            var battery = items[i];
            if (battery instanceof Flux f && f.consFlux().capacity > 0f) {
                var flux = f.flux();
                var cons = f.consFlux();
                flux.fluxAmount += (cons.capacity - flux.fluxAmount) * chargedPercent;
            }
        }
    }

    public float getChangeAmount() {
        float totalCapacity = 0f;
        var items = all.items;
        for (int i = 0; i < all.size; i++) {
            var battery = items[i];
            if (battery instanceof Flux f) {
                totalCapacity += f.outputAmount() - f.consumerAmount();
            }
        }
        return totalCapacity;
    }

    // ========== 更新 ==========
    public void update() {
        if (!Vars.state.isGame()) {
            deprecate();
            return;
        }

        if (lastChange != world.tileChanges) {
            lastChange = world.tileChanges;
            updateGraph();
        }

        float amount = getChangeAmount();
        changeCapacity(amount);
        if (isOverload()) {
            all.each(b -> MDEvents.overload.addUnique(b));
        }
    }

    public void updateGraph() {
        if (!init) return;

        if (all.size == 0) {
            deprecate();
            return;
        }

        Building start = all.get(0);
        if (start.dead || world.build(start.pos()) != start) {
            // 起始节点失效，找一个有效的
            start = findValidStart();
            if (start == null) {
                deprecate();
                return;
            }
        }

        bfs(start);
    }

    private Building findValidStart() {
        var items = all.items;
        for (int i = 0; i < all.size; i++) {
            Building b = items[i];
            if (!b.dead && world.build(b.pos()) == b) {
                return b;
            }
        }
        return null;
    }

    // ========== 存档兼容 ==========
    public void saveLoad() {
        for (Building b : all) {
            Building nowb = world.build(b.pos());
            FluxModule flux = flux(nowb);
            if (flux != null) {
                flux.graph = new FluxGraph();
                flux.graph.init(nowb);
            }
        }
        deprecate();
    }

    // ========== 拆除处理（对齐原版 PowerGraph.remove） ==========
    public void remove(Building tile) {
        if (!(tile instanceof Flux f)) return;

        // 收集邻居，使用局部变量
        Seq<Building> neighbors = new Seq<>(false, 4, Building.class);
        f.FluxConnections(neighbors);

        for (int i = 0; i < neighbors.size; i++) {
            Building other = neighbors.items[i];
            if (!(other instanceof Flux of)) continue;
            Debug.cry(other);

            FluxModule oflux = of.flux();
            if (oflux == null || oflux.graph != this) continue;

            // 创建新图，BFS 填充
            FluxGraph graph = new FluxGraph();
            graph.bfs(other, tile, c->{
                Debug.cry(c,Pal.heal);
            });
        }

        deprecate();
    }

    // ========== 合并图 ==========
    public void addGraph(FluxGraph graph) {
        if (graph == this) return;

        // 合并到更大的图
        if (graph.all.size > all.size) {
            graph.addGraph(this);
            return;
        }

        graph.deprecate();
        for (Building tile : graph.all) {
            add(tile);
        }
        init();
    }

    @Override
    public String toString() {
        return "FluxGraph{" +
                "all=" + all.size +
                ", storage=" + storage.size +
                ", consumer=" + consumer.size +
                ", deprecate=" + deprecate +
                '}';
    }
}