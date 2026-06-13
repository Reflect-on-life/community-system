"""
YOLOv11 物体检测微服务
启动方式: pip install ultralytics flask && python yolo_service.py
"""
import base64
import io
import json
import sys
import traceback
from flask import Flask, request, jsonify
from PIL import Image

app = Flask(__name__)

# 延迟加载模型，首次调用时加载
model = None

def get_model():
    global model
    if model is None:
        try:
            from ultralytics import YOLO
            # 使用 YOLOv11n（nano 版本，速度快，适合 CPU）
            model = YOLO("yolo11n.pt")
            print("YOLOv11n 模型加载成功", flush=True)
        except Exception as e:
            print(f"YOLOv11 加载失败: {e}，回退到 yolov8n", flush=True)
            from ultralytics import YOLO
            model = YOLO("yolov8n.pt")
            print("YOLOv8n 模型加载成功（回退）", flush=True)
    return model

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "service": "yolo-detector"})

@app.route("/detect", methods=["POST"])
def detect():
    try:
        data = request.get_json()
        if not data or "image" not in data:
            return jsonify({"error": "缺少 image 字段"}), 400

        image_b64 = data["image"]
        image_bytes = base64.b64decode(image_b64)
        image = Image.open(io.BytesIO(image_bytes))

        yolo = get_model()
        results = yolo(image, conf=0.3, verbose=False)

        detections = []
        if results and len(results) > 0:
            for box in results[0].boxes:
                cls_id = int(box.cls[0].item())
                conf = float(box.conf[0].item())
                label = results[0].names.get(cls_id, f"class_{cls_id}")
                detections.append({
                    "label": label,
                    "confidence": round(conf, 3),
                    "class_id": cls_id
                })

        return jsonify({"detections": detections, "count": len(detections)})

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    print("YOLO 检测服务启动在 http://0.0.0.0:5000", flush=True)
    app.run(host="0.0.0.0", port=5000, debug=False)


