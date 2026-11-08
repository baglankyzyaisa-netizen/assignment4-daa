import json, random, subprocess, sys, itertools
sys.path.insert(0,'tools')
random.seed(7)
def rs(n,al='ab'): return ''.join(random.choice(al) for _ in range(n))
reqs=[];exp=[]
def brute_search(t,p): return [i for i in range(len(t)-len(p)+1) if t[i:i+len(p)]==p]
for _ in range(300):
    t,p=rs(random.randint(0,12)),rs(random.randint(0,4))
    for k in('naive','kmp','rk'): reqs.append({'task':k,'text':t,'pattern':p}); exp.append({'positions':brute_search(t,p)})
    a,b=rs(random.randint(0,10),'abc'),rs(random.randint(0,10),'abc')
    reqs.append({'task':'lcs','a':a,'b':b})
    n=len(a);ex=[[0]*(len(b)+1) for _ in range(n+1)]
    for i in range(n):
        for j in range(len(b)): ex[i+1][j+1]=ex[i][j]+1 if a[i]==b[j] else max(ex[i][j+1],ex[i+1][j])
    exp.append({'length':ex[n][len(b)]})
    reqs.append({'task':'suffix','text':t})
    sa=sorted(range(len(t)),key=lambda i:t[i:]);exp.append({'sa':sa})
    pats=[rs(random.randint(1,3)) for _ in range(random.randint(0,4))];txts=[rs(random.randint(0,10)) for _ in range(3)]
    reqs.append({'task':'aho','patterns':pats,'texts':txts})
    exp.append({'counts':[[len(brute_search(x,q)) for q in pats] for x in txts]})
    sz=[random.randint(0,9) for _ in range(random.randint(0,7))]
    reqs.append({'task':'merge','sizes':sz})
    from functools import lru_cache
    @lru_cache(None)
    def f(l,r):
        if l>=r:return 0
        return min(f(l,k)+f(k+1,r) for k in range(l,r))+sum(sz[l:r+1])
    exp.append({'cost':f(0,len(sz)-1) if sz else 0})
    n=random.randint(0,9);w=[random.randint(-5,9) for _ in range(n)];ed=[[i,random.randint(0,i-1)] for i in range(1,n)]
    reqs.append({'task':'tree','n':n,'weights':w,'edges':ed})
    adj={(u,v) for u,v in ed}
    best=0
    for m in range(1<<n):
        if all(not(m>>u&1 and m>>v&1) for u,v in ed): best=max(best,sum(w[i] for i in range(n) if m>>i&1))
    exp.append({'weight':best})
open(sys.argv[1],'w').write(''.join(json.dumps(r)+'\n' for r in reqs))
out=subprocess.run(['java','-cp','Java/build','Main'],input=open(sys.argv[1]).read(),capture_output=True,text=True).stdout.splitlines()
bad=0
for r,e,o in zip(reqs,exp,out):
    o=json.loads(o)
    ok=all(o.get(k)==v for k,v in e.items()) if r['task'] not in('lcs','suffix','tree','merge') or True else True
    if r['task']=='suffix':
        t=r['text'];ok=o['sa']==e['sa'] and all(o['lcp'][i]==(lambda a,b:next((k for k in range(min(len(a),len(b))) if a[k]!=b[k]),min(len(a),len(b))))(t[o['sa'][i-1]:],t[o['sa'][i]:]) for i in range(1,len(t))) and (not t or len(o['repeated'])==max(o['lcp']))
    if not ok: bad+=1;print('BAD',r,e,o)
print('checked',len(reqs),'bad',bad,len(out))
