"""Run from repo root after building: python tests/student/run_cases.py"""
import json, subprocess
cases = json.load(open('tests/student/cases.json'))
data = ''.join(json.dumps(c['input']) + '\n' for c in cases)
out = subprocess.run(['java', '-cp', 'Java/build', 'Main'], input=data, capture_output=True, text=True).stdout.splitlines()
bad = 0
for c, line in zip(cases, out):
    got = json.loads(line)
    ok = all(got.get(k) == v for k, v in c['expect'].items())
    print('PASS' if ok else 'FAIL', c['name'], '' if ok else got)
    bad += not ok
raise SystemExit(bad)
