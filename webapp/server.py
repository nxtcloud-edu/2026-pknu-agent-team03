"""
TimeBack 백엔드 API 서버 (Flask)
목업 데이터 기반 — 메모리에서 CRUD 가능
"""
from flask import Flask, jsonify, request, send_from_directory
import os, time, uuid, copy
from datetime import datetime

app = Flask(__name__, static_folder='.', static_url_path='')

# ══════════════════════════════════════════════
# 목업 데이터 (MockScreenTimeData.java 기반)
# ══════════════════════════════════════════════

BASE_DAY = 1733670000000  # 2024-12-09 00:00 KST
ONE_DAY = 86400000
ONE_HOUR = 3600000
ONE_MIN = 60000

def make_item(id, app, pkg, activity, day, hour, minute, dur_min, cls):
    start = BASE_DAY + day * ONE_DAY + hour * ONE_HOUR + minute * ONE_MIN
    return {
        "id": id, "app": app, "package": pkg, "activity": activity,
        "startAt": start, "endAt": start + dur_min * ONE_MIN,
        "durationMin": dur_min, "classification": cls,
        "userConfirmed": False
    }

TIMELINE_DATA = [
    # Day 0 월
    [make_item("d1-1","Instagram","com.instagram.android","피드 스크롤",0,7,30,45,"WASTE"),
     make_item("d1-2","Chrome","com.android.chrome","채용공고 검색",0,9,0,90,"PRODUCTIVE"),
     make_item("d1-3","TikTok","com.zhiliaoapp.musically","숏폼 시청",0,11,0,60,"WASTE"),
     make_item("d1-4","Google Docs","com.google.android.apps.docs","자기소개서 작성",0,13,0,120,"PRODUCTIVE"),
     make_item("d1-5","X","com.twitter.android","트윗 브라우징",0,15,30,40,"LEISURE"),
     make_item("d1-6","Instagram","com.instagram.android","릴스 시청",0,18,0,90,"WASTE"),
     make_item("d1-7","YouTube","com.google.android.youtube","면접 팁 영상",0,20,0,60,"MIXED")],
    # Day 1 화
    [make_item("d2-1","Instagram","com.instagram.android","스토리 확인",1,8,0,30,"WASTE"),
     make_item("d2-2","Notion","notion.id","면접 질문 정리",1,9,0,120,"PRODUCTIVE"),
     make_item("d2-3","TikTok","com.zhiliaoapp.musically","취준생 밈 시청",1,12,0,50,"WASTE"),
     make_item("d2-4","Chrome","com.android.chrome","포트폴리오 참고",1,14,0,90,"PRODUCTIVE"),
     make_item("d2-5","X","com.twitter.android","IT 뉴스 읽기",1,16,0,35,"LEISURE"),
     make_item("d2-6","Instagram","com.instagram.android","릴스 무한 스크롤",1,19,0,80,"WASTE"),
     make_item("d2-7","TikTok","com.zhiliaoapp.musically","야간 숏폼",1,22,0,45,"WASTE")],
    # Day 2 수
    [make_item("d3-1","TikTok","com.zhiliaoapp.musically","아침 숏폼",2,7,0,60,"WASTE"),
     make_item("d3-2","Instagram","com.instagram.android","탐색 탭",2,9,0,40,"WASTE"),
     make_item("d3-3","VS Code","com.visualstudio.code","사이드 프로젝트",2,10,0,150,"PRODUCTIVE"),
     make_item("d3-4","X","com.twitter.android","개발자 트위터",2,13,0,45,"MIXED"),
     make_item("d3-5","Instagram","com.instagram.android","DM 확인+피드",2,15,0,55,"WASTE"),
     make_item("d3-6","Netflix","com.netflix.mediaclient","드라마 시청",2,20,0,120,"LEISURE")],
    # Day 3 목
    [make_item("d4-1","Chrome","com.android.chrome","알고리즘 문제",3,8,0,120,"PRODUCTIVE"),
     make_item("d4-2","Instagram","com.instagram.android","점심 인스타",3,12,0,25,"WASTE"),
     make_item("d4-3","Notion","notion.id","프로젝트 문서화",3,13,0,90,"PRODUCTIVE"),
     make_item("d4-4","TikTok","com.zhiliaoapp.musically","코딩 팁 영상",3,15,30,30,"MIXED"),
     make_item("d4-5","Chrome","com.android.chrome","기업 분석",3,16,30,60,"PRODUCTIVE"),
     make_item("d4-6","X","com.twitter.android","저녁 트위터",3,20,0,40,"LEISURE"),
     make_item("d4-7","Instagram","com.instagram.android","자기 전 릴스",3,23,0,35,"WASTE")],
    # Day 4 금
    [make_item("d5-1","Instagram","com.instagram.android","모닝 피드",4,8,0,50,"WASTE"),
     make_item("d5-2","TikTok","com.zhiliaoapp.musically","오전 숏폼",4,9,30,70,"WASTE"),
     make_item("d5-3","Chrome","com.android.chrome","지원서 제출",4,11,30,60,"PRODUCTIVE"),
     make_item("d5-4","X","com.twitter.android","트렌드 확인",4,13,0,45,"LEISURE"),
     make_item("d5-5","Instagram","com.instagram.android","친구 스토리",4,14,30,40,"LEISURE"),
     make_item("d5-6","TikTok","com.zhiliaoapp.musically","밤 숏폼 루프",4,21,0,100,"WASTE")],
    # Day 5 토
    [make_item("d6-1","Instagram","com.instagram.android","늦잠 후 인스타",5,10,0,60,"WASTE"),
     make_item("d6-2","TikTok","com.zhiliaoapp.musically","점심까지 숏폼",5,11,30,90,"WASTE"),
     make_item("d6-3","YouTube","com.google.android.youtube","개발 강의",5,14,0,120,"PRODUCTIVE"),
     make_item("d6-4","X","com.twitter.android","주말 트위터",5,17,0,50,"LEISURE"),
     make_item("d6-5","Instagram","com.instagram.android","저녁 릴스",5,19,30,75,"WASTE"),
     make_item("d6-6","TikTok","com.zhiliaoapp.musically","심야 숏폼",5,23,0,60,"WASTE")],
    # Day 6 일
    [make_item("d7-1","Instagram","com.instagram.android","아침 확인",6,9,0,20,"WASTE"),
     make_item("d7-2","Notion","notion.id","주간 회고",6,10,0,90,"PRODUCTIVE"),
     make_item("d7-3","Chrome","com.android.chrome","시간관리 앱 리서치",6,12,0,45,"PRODUCTIVE"),
     make_item("d7-4","TikTok","com.zhiliaoapp.musically","잠깐 숏폼",6,14,0,25,"WASTE"),
     make_item("d7-5","Chrome","com.android.chrome","주간 계획 수립",6,15,0,60,"PRODUCTIVE"),
     make_item("d7-6","X","com.twitter.android","개발 커뮤니티",6,17,0,30,"MIXED"),
     make_item("d7-7","Instagram","com.instagram.android","자기 전 짧게",6,22,0,15,"WASTE")]
]

GOALS = [
    {"id":"g1","name":"코딩 공부","targetMin":300,"doneMin":210},
    {"id":"g2","name":"포트폴리오 정리","targetMin":180,"doneMin":120},
    {"id":"g3","name":"운동","targetMin":150,"doneMin":45},
    {"id":"g4","name":"독서","targetMin":120,"doneMin":30}
]

BACKUP_STATE = {"status":"SYNCED","lastSyncAt":int(time.time()*1000),"pendingCount":0}
RETENTION = {"selection":"30_DAYS"}

# ══════════════════════════════════════════════
# API Routes
# ══════════════════════════════════════════════

@app.route('/')
def index():
    return send_from_directory('.', 'index.html')

# --- Timeline ---
@app.get('/api/timeline')
def get_timeline():
    day = request.args.get('day', 6, type=int)
    if 0 <= day <= 6:
        return jsonify({"day": day, "items": TIMELINE_DATA[day]})
    return jsonify({"error": "day must be 0-6"}), 400

@app.get('/api/timeline/all')
def get_all_timeline():
    return jsonify({"days": TIMELINE_DATA})

# --- Classification 변경 ---
@app.put('/api/timeline/<item_id>/classify')
def update_classification(item_id):
    data = request.get_json()
    new_cls = data.get("classification")
    new_activity = data.get("activity")
    if new_cls and new_cls not in ("WASTE","PRODUCTIVE","LEISURE","MIXED","NEUTRAL"):
        return jsonify({"error":"invalid classification"}), 400
    for day in TIMELINE_DATA:
        for item in day:
            if item["id"] == item_id:
                if new_cls:
                    item["classification"] = new_cls
                if new_activity is not None:
                    item["activity"] = new_activity
                item["userConfirmed"] = True
                return jsonify(item)
    return jsonify({"error":"not found"}), 404

# --- Goals ---
@app.get('/api/goals')
def get_goals():
    return jsonify(GOALS)

@app.post('/api/goals')
def add_goal():
    data = request.get_json()
    goal = {"id": str(uuid.uuid4())[:8], "name": data["name"], "targetMin": data.get("targetMin",60), "doneMin": 0}
    GOALS.append(goal)
    return jsonify(goal), 201

@app.put('/api/goals/<goal_id>/record')
def record_time(goal_id):
    data = request.get_json()
    minutes = data.get("minutes", 0)
    for g in GOALS:
        if g["id"] == goal_id:
            g["doneMin"] = max(0, g["doneMin"] + minutes)  # 음수 허용하되 0 이하로 안 감
            return jsonify(g)
    return jsonify({"error":"not found"}), 404

@app.delete('/api/goals/<goal_id>')
def delete_goal(goal_id):
    global GOALS
    GOALS = [g for g in GOALS if g["id"] != goal_id]
    return jsonify({"deleted": goal_id})

# --- Metrics ---
@app.get('/api/metrics/weekly')
def weekly_metrics():
    # 현재 요일 기준 (월=0, 화=1, ..., 일=6)
    today_idx = datetime.now().weekday()  # Python: 월=0 ... 일=6
    total_waste = sum(i["durationMin"] for d in TIMELINE_DATA for i in d if i["classification"]=="WASTE")
    total_prod = sum(i["durationMin"] for d in TIMELINE_DATA for i in d if i["classification"]=="PRODUCTIVE")
    total_leisure = sum(i["durationMin"] for d in TIMELINE_DATA for i in d if i["classification"]=="LEISURE")
    baseline = total_waste // 7
    today_waste = sum(i["durationMin"] for i in TIMELINE_DATA[today_idx] if i["classification"]=="WASTE")
    today_total = sum(i["durationMin"] for i in TIMELINE_DATA[today_idx])
    saved = max(0, baseline - today_waste)
    recovered = sum(g["doneMin"] for g in GOALS)
    recovery_rate = round(recovered / saved * 100) if saved > 0 else 0
    return jsonify({
        "totalWasteMin": total_waste, "totalProductiveMin": total_prod,
        "totalLeisureMin": total_leisure, "baselineMin": baseline,
        "todayWasteMin": today_waste, "todayTotalMin": today_total, "savedMin": saved,
        "recoveredMin": recovered, "recoveryRate": recovery_rate,
        "dailyWaste": [sum(i["durationMin"] for i in d if i["classification"]=="WASTE") for d in TIMELINE_DATA],
        "todayIndex": today_idx
    })

# --- Backup (M4) ---
@app.get('/api/backup/status')
def backup_status():
    return jsonify(BACKUP_STATE)

@app.post('/api/backup/sync')
def trigger_sync():
    BACKUP_STATE["lastSyncAt"] = int(time.time()*1000)
    BACKUP_STATE["status"] = "SYNCED"
    BACKUP_STATE["pendingCount"] = 0
    return jsonify(BACKUP_STATE)

@app.get('/api/retention')
def get_retention():
    return jsonify(RETENTION)

@app.put('/api/retention')
def set_retention():
    data = request.get_json()
    RETENTION["selection"] = data.get("selection","30_DAYS")
    return jsonify(RETENTION)

@app.post('/api/deletion')
def request_deletion():
    job_id = str(uuid.uuid4())[:8]
    # 실제로는 전체 삭제 — 여기선 시뮬레이션
    return jsonify({"jobId": job_id, "deviceStatus":"COMPLETED","serverStatus":"COMPLETED","completedAt":int(time.time()*1000)})

# ══════════════════════════════════════════════
if __name__ == '__main__':
    print("TimeBack API 서버 시작: http://localhost:5000")
    app.run(debug=True, port=5000)
