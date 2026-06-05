<?php
include_once 'service/PositionService.php';

header('Content-Type: application/json');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    try {
        $latitude = $_POST['latitude'] ?? null;
        $longitude = $_POST['longitude'] ?? null;
        $datePosition = $_POST['date_position'] ?? date('Y-m-d H:i:s');
        $imei = $_POST['imei'] ?? 'unknown';

        if ($latitude === null || $longitude === null) {
            echo json_encode(["status" => "error", "message" => "Missing coordinates"]);
            exit;
        }

        $service = new PositionService();
        $position = new Position(null, $latitude, $longitude, $datePosition, $imei);

        if ($service->create($position)) {
            echo json_encode([
                "status" => "success",
                "message" => "Position tracked successfully",
                "received" => [
                    "lat" => $latitude,
                    "lon" => $longitude,
                    "imei" => $imei
                ]
            ]);
        }
    } catch (Exception $e) {
        echo json_encode(["status" => "error", "message" => $e->getMessage()]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Only POST requests allowed"]);
}
?>