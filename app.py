# FastAPI application for fingerprint matching
# To run the app, use the command:
# uvicorn app:app --reload

# adb -s 120703145D125101 reverse tcp:8000 tcp:8000
# first run in cmd shell

# uvicorn app:app --host 0.0.0.0 --port 8000

from fastapi import FastAPI, UploadFile, File, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
import cv2
import numpy as np
import json
import os

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.mount("/static", StaticFiles(directory="database/photos"), name="static")

MATCH_THRESHOLD = 25.0  # percent

def read_image(file_bytes: bytes) -> np.ndarray:
    img_array = np.asarray(bytearray(file_bytes), dtype=np.uint8)
    img = cv2.imdecode(img_array, cv2.IMREAD_GRAYSCALE)
    return apply_clahe(img)

def apply_clahe(img: np.ndarray) -> np.ndarray:
    if img is None:
        return None
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    return clahe.apply(img)

def compare_fingerprints(img1: np.ndarray, img2: np.ndarray) -> float:
    orb = cv2.ORB_create(nfeatures=1000)
    kp1, des1 = orb.detectAndCompute(img1, None)
    kp2, des2 = orb.detectAndCompute(img2, None)

    if des1 is None or des2 is None:
        return 0.0

    bf = cv2.BFMatcher(cv2.NORM_HAMMING)
    matches = bf.knnMatch(des1, des2, k=2)
    good_matches = [m for m, n in matches if m.distance < 0.75 * n.distance]

    total_kp = min(len(kp1), len(kp2))
    if total_kp == 0:
        return 0.0
    return len(good_matches) / total_kp * 100

@app.post("/match-fingerprint/")
async def match_fingerprint(file: UploadFile = File(...), request: Request = None):
    try:
        contents = await file.read()
        if not contents:
            raise ValueError("Empty file.")
        uploaded_img = read_image(contents)
        if uploaded_img is None:
            raise ValueError("Invalid image.")
    except Exception:
        return JSONResponse(
            content={"status": "error", "message": "Invalid or unreadable image."},
            status_code=400,
        )

    try:
        with open("database/profiles.json") as f:
            profiles = json.load(f)
    except Exception as e:
        print(f"Error loading profiles.json: {e}")
        return JSONResponse(
            content={"status": "error", "message": "Failed to load profiles."},
            status_code=500,
        )

    best_score = 0
    matched_profile = None

    for profile in profiles:
        db_img_path = os.path.join("database/fingerprints", profile["fingerprint"])
        db_img = cv2.imread(db_img_path, cv2.IMREAD_GRAYSCALE)
        if db_img is None:
            continue
        db_img = apply_clahe(db_img)
        score = compare_fingerprints(uploaded_img, db_img)
        print(f"Compared with {profile['name']}, Score: {score:.2f}")

        if score > best_score and score >= MATCH_THRESHOLD:
            best_score = score
            matched_profile = profile.copy()
            host_url = str(request.base_url) if request else "http://localhost:8000/"
            matched_profile["photo"] = f"{host_url}static/{profile['photo']}"

    if matched_profile:
        return JSONResponse(content={"status": "success", "data": matched_profile}, status_code=200)
    else:
        host_url = str(request.base_url) if request else "http://localhost:8000/"
        return JSONResponse(
            content={
                "status": "fail",
                "message": "No match found",
                "data": {
                    "name": "Not Found",
                    "age": None,
                    "photo": f"{host_url}static/image.png"
                }
            },
            status_code=404,
        )
