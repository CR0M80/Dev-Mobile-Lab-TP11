<?php
include_once 'dao/IDao.php';
include_once 'classe/Position.php';
include_once 'connexion/Connexion.php';

class PositionService implements IDao {
    private $connexion;

    public function __construct() {
        $db = new Connexion();
        $this->connexion = $db->getConnexion();
    }

    public function create($position) {
        $sql = "INSERT INTO position(latitude, longitude, date_position, imei)
                VALUES(:latitude, :longitude, :date_position, :imei)";
        $stmt = $this->connexion->prepare($sql);
        $stmt->execute([
            ':latitude' => $position->getLatitude(),
            ':longitude' => $position->getLongitude(),
            ':date_position' => $position->getDatePosition(),
            ':imei' => $position->getImei()
        ]);
        return true;
    }

    public function update($obj) {}
    public function delete($obj) {}
    public function getById($obj) {}
    public function getAll() {
        $sql = "SELECT * FROM position ORDER BY date_position DESC";
        $stmt = $this->connexion->query($sql);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}
?>