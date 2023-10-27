package edu.gatech.chai.VRDR.model;

//import java.util.concurrent.ConcurrentBag;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SymbolTable {
    //private List<TableEntry> _entries = new ArrayList<>();
    private ConcurrentLinkedQueue<TableEntry> _entries = new ConcurrentLinkedQueue<>();
    private SymbolTable Parent;
    public SymbolTable() {

    }

    public SymbolTable(SymbolTable parent) {
        Parent = parent;
    }


    private class TableEntry {
        public CallSignature Signature;
        public Invokee Body;

        public TableEntry(CallSignature signature, Invokee body) {
            Signature = signature;
            Body = body;
        }
    }


    public void Add(CallSignature signature, Invokee body) {
        _entries.add(new TableEntry(signature, body));
    }

    public int count() {
        int cnt = _entries.size();
        if (Parent != null) cnt += Parent.count();

        return cnt;
    }


//    public Invokee First() {
//        return _entries.size() > 0 ? _entries.peek().Body : (Parent != null ? Parent.First() : null);
//    }

    private Invokee First() {
        return _entries.isEmpty() ? (Parent != null ? Parent.First() : null) : _entries.peek().Body;//get(0).Body;
    }

    public SymbolTable Filter(String name, int argCount) {
        SymbolTable result = new SymbolTable();
//        result._entries = new ArrayList<>(_entries);
//        result._entries.removeIf(e -> !e.Signature.Matches(name, argCount));
        for (TableEntry e : _entries) {
            if (e.Signature.Matches(name, argCount)) {
                result.Add(e.Signature, e.Body);
            }
        }

        if (Parent != null) {
            result.Parent = Parent.Filter(name, argCount);
        }

        return result;
    }

    public Invokee DynamicGet(String name, List<Object> args) {
        List<TableEntry> exactMatches = new ArrayList<TableEntry>();
        for (TableEntry e : _entries) {
            if (e.Signature.DynamicExactMatches(name, args)) {
                exactMatches.add(e);
            }
        }
//        TableEntry entry = null;
//        if (entry == null && Parent != null)
//            return Parent.DynamicGet(name, args);
//        return entry != null ? entry.Body : null;
        TableEntry entry = exactMatches.isEmpty() ? null : exactMatches.get(0);
        for (TableEntry e : exactMatches) {
            if (entry == null || e.Signature.ArgumentTypes.length < entry.Signature.ArgumentTypes.length) {
                entry = e;
            }
        }
        if (entry == null) {
            for (TableEntry e : _entries) {
                if (e.Signature.DynamicMatches(name, args)) {
                    if (entry == null || e.Signature.ArgumentTypes.length < entry.Signature.ArgumentTypes.length) {
                        entry = e;
                    }
                }
            }
        }
        if (entry == null && Parent != null) {
            return Parent.DynamicGet(name, args);
        }

        return entry.Body;
    }
}
