package data.scripts.weapons;

import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_PandoraEmitter implements EveryFrameWeaponEffectPlugin {

    private static final String WEAPON_ID = "ms_pandora";
    private static final String WEAPON_2 = "ms_derazhoObject";
    private static final String TARGET_ID = "ms_pandoraEmitterTarg";
    private static final float EMITTER_THICKNESS = 8f;
    private static final float E_DARK_THICKNESS = 4f;
    private static final Color EMITTER_FRINGE = new Color(125, 155, 115, 150);
    private static final Color EMITTER_CORE = new Color(165, 215, 145, 255);
    private static final Color E_DARK_FRINGE = new Color(125, 115, 155, 255);
    private static final Color E_DARK_CORE = new Color(65, 45, 115, 155);
    private boolean hasChecked = false, hasEye = false;
    private WeaponAPI firingEye = null;
    private WeaponAPI closestEye = null;
    private FollowWeaponCombatEntity eyeFollower = null;
    private CombatEntityAPI activeArc = null;

    public boolean checkForSeeingEye(WeaponAPI emitter) {
        ShipAPI ship = emitter.getShip();
        List<WeaponAPI> weapons = ship.getAllWeapons();
        float closestDistance = Float.MAX_VALUE;

        for (WeaponAPI weapon : weapons) {
            if (TARGET_ID.equals(weapon.getId())) {
                if (closestEye == null || MathUtils.getDistanceSquared(weapon.getLocation(), emitter.getLocation()) < closestDistance) {
                    closestEye = weapon;
                    closestDistance = MathUtils.getDistanceSquared(emitter.getLocation(), weapon.getLocation());
                }
            }
            if (WEAPON_ID.equals(weapon.getId()) || WEAPON_2.equals(weapon.getId())) {
                if (firingEye == null) {
                    firingEye = weapon;
                }
            }
        }

        if (closestEye != null && firingEye != null) {
            eyeFollower = new FollowWeaponCombatEntity(closestEye);
            return true;
        }

        return false;
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!hasChecked) {
            hasEye = checkForSeeingEye(weapon);
            hasChecked = true;
        }

        if (!hasEye || engine.isPaused() || (activeArc != null && engine.isEntityInPlay(activeArc))) {
            return;
        }

        if (firingEye.isFiring() && firingEye.getChargeLevel() <= 5.5) {
            if (WEAPON_2.equals(firingEye.getId())) {
                activeArc = engine.spawnEmpArc(weapon.getShip(),
                        weapon.getLocation(), weapon.getShip(),
                        eyeFollower, DamageType.OTHER, 0f, 0f, 5000f,
                        null, E_DARK_THICKNESS, E_DARK_FRINGE, E_DARK_CORE);
            }
            else {
                activeArc = engine.spawnEmpArc(weapon.getShip(),
                        weapon.getLocation(), weapon.getShip(),
                        eyeFollower, DamageType.OTHER, 0f, 0f, 5000f,
                        null, EMITTER_THICKNESS, EMITTER_FRINGE, EMITTER_CORE); 
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="FollowWeaponCombatEntity">
    public static class FollowWeaponCombatEntity implements CombatEntityAPI {

        private final WeaponAPI weapon;

        public FollowWeaponCombatEntity(WeaponAPI weapon) {
            this.weapon = weapon;
        }

        @Override
        public Vector2f getLocation() {
            return weapon.getLocation();
        }

        @Override
        public Vector2f getVelocity() {
            return null;
        }

        @Override
        public float getFacing() {
            return weapon.getArcFacing();
        }

        @Override
        public void setFacing(float facing) {
        }

        @Override
        public float getAngularVelocity() {
            return 0f;
        }

        @Override
        public void setAngularVelocity(float angVel) {
        }

        @Override
        public int getOwner() {
            return weapon.getShip().getOwner();
        }

        @Override
        public void setOwner(int owner) {
        }

        @Override
        public float getCollisionRadius() {
            return 0f;
        }

        @Override
        public CollisionClass getCollisionClass() {
            return null;
        }

        @Override
        public void setCollisionClass(CollisionClass collisionClass) {
        }

        @Override
        public float getMass() {
            return 0f;
        }

        @Override
        public void setMass(float mass) {
        }

        @Override
        public BoundsAPI getExactBounds() {
            return null;
        }

        @Override
        public ShieldAPI getShield() {
            return null;
        }

        @Override
        public float getHullLevel() {
            return 0f;
        }

        @Override
        public float getHitpoints() {
            return 0f;
        }

        @Override
        public float getMaxHitpoints() {
            return 0f;
        }

        @Override
        public void setCollisionRadius(float f) {
        }

        @Override
        public Object getAI() {
            return null;
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public void setCustomData(String string, Object o) { }

        @Override
        public void removeCustomData(String string) { }

        @Override
        public Map<String, Object> getCustomData() {
            return null;
        }

        @Override
        public void setHitpoints(float hp) {
        }

        @Override
        public boolean isPointInBounds(Vector2f p) {
            return false;
        }

        @Override
        public boolean wasRemoved() {
            return false;
        }
    }
    //</editor-fold>
}