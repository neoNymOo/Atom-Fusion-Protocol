package com.nymoo.afp.common.render.model.armor;

import com.nymoo.afp.common.render.core.ModelRendererObj;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Абстрактная модель брони для силовой брони мода Atom Fusion Protocol.
 * Расширяет стандартную модель бипеда для поддержки кастомных частей брони и анимаций.
 */
public class AbstractArmorModel extends ModelBiped {

    /**
     * Модель головы брони
     */
    public ModelRendererObj head;
    /**
     * Модель тела брони
     */
    public ModelRendererObj body;
    /**
     * Модель левой руки брони
     */
    public ModelRendererObj leftArm;
    /**
     * Модель правой руки брони
     */
    public ModelRendererObj rightArm;
    /**
     * Модель левой ноги брони
     */
    public ModelRendererObj leftLeg;
    /**
     * Модель правой ноги брони
     */
    public ModelRendererObj rightLeg;
    /**
     * Модель левой ступни брони
     */
    public ModelRendererObj leftFoot;
    /**
     * Модель правой ступни брони
     */
    public ModelRendererObj rightFoot;
    /**
     * Тип брони для определения специфических свойств
     */
    int type;

    /**
     * Создает новую абстрактную модель брони с указанным типом.
     * Инициализирует все части модели и устанавливает точки вращения.
     *
     * @param type Тип брони для создания модели
     */
    public AbstractArmorModel(int type) {
        this.type = type;

        head = new ModelRendererObj(null);
        body = new ModelRendererObj(null);
        leftArm = new ModelRendererObj(null).setRotationPoint(-5.0F, 2.0F, 0.0F);
        rightArm = new ModelRendererObj(null).setRotationPoint(5.0F, 2.0F, 0.0F);
        leftLeg = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
        rightLeg = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
        leftFoot = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
        rightFoot = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    /**
     * Устанавливает углы поворота для всех частей модели на основе состояния сущности.
     * Синхронизирует кастомные части модели со стандартными частями бипеда и обрабатывает особые случаи.
     *
     * @param walkCycle     Цикл ходьбы для анимации
     * @param walkAmplitude Амплитуда ходьбы для анимации
     * @param idleCycle     Цикл простоя для анимации
     * @param headYaw       Угол поворота головы по горизонтали
     * @param headPitch     Угол поворота головы по вертикали
     * @param scale         Масштабный коэффициент модели
     * @param entity        Сущность, для которой устанавливаются углы
     */
    @Override
    public void setRotationAngles(float walkCycle, float walkAmplitude, float idleCycle, float headYaw, float headPitch, float scale, Entity entity) {

        super.setRotationAngles(walkCycle, walkAmplitude, idleCycle, headYaw, headPitch, scale, entity);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            this.isSneak = player.isSneaking();
        }

        // Синхронизация позиций и углов кастомных частей со стандартными частями бипеда
        this.head.rotationPointX = this.bipedHead.rotationPointX;
        this.head.rotationPointY = this.bipedHead.rotationPointY;
        this.head.rotationPointY = this.bipedHead.rotationPointY;
        this.head.rotateAngleX = this.bipedHead.rotateAngleX;
        this.head.rotateAngleY = this.bipedHead.rotateAngleY;
        this.head.rotateAngleZ = this.bipedHead.rotateAngleZ;

        this.body.rotationPointX = this.bipedBody.rotationPointX;
        this.body.rotationPointY = this.bipedBody.rotationPointY;
        this.body.rotationPointZ = this.bipedBody.rotationPointZ;
        this.body.rotateAngleX = this.bipedBody.rotateAngleX;
        this.body.rotateAngleY = this.bipedBody.rotateAngleY;
        this.body.rotateAngleZ = this.bipedBody.rotateAngleZ;

        this.leftArm.rotationPointX = this.bipedLeftArm.rotationPointX;
        this.leftArm.rotationPointY = this.bipedLeftArm.rotationPointY;
        this.leftArm.rotationPointZ = this.bipedLeftArm.rotationPointZ;
        this.leftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX;
        this.leftArm.rotateAngleY = this.bipedLeftArm.rotateAngleY;
        this.leftArm.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;

        this.rightArm.rotationPointX = this.bipedRightArm.rotationPointX;
        this.rightArm.rotationPointY = this.bipedRightArm.rotationPointY;
        this.rightArm.rotationPointZ = this.bipedRightArm.rotationPointZ;
        this.rightArm.rotateAngleX = this.bipedRightArm.rotateAngleX;
        this.rightArm.rotateAngleY = this.bipedRightArm.rotateAngleY;
        this.rightArm.rotateAngleZ = this.bipedRightArm.rotateAngleZ;

        this.leftLeg.rotationPointX = this.bipedLeftLeg.rotationPointX;
        this.leftLeg.rotationPointY = this.bipedLeftLeg.rotationPointY;
        this.leftLeg.rotationPointZ = this.bipedLeftLeg.rotationPointZ;
        this.leftLeg.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
        this.leftLeg.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
        this.leftLeg.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;

        this.rightLeg.rotationPointX = this.bipedRightLeg.rotationPointX;
        this.rightLeg.rotationPointY = this.bipedRightLeg.rotationPointY;
        this.rightLeg.rotationPointZ = this.bipedRightLeg.rotationPointZ;
        this.rightLeg.rotateAngleX = this.bipedRightLeg.rotateAngleX;
        this.rightLeg.rotateAngleY = this.bipedRightLeg.rotateAngleY;
        this.rightLeg.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;

        this.leftFoot.rotationPointX = this.bipedLeftLeg.rotationPointX;
        this.leftFoot.rotationPointY = this.bipedLeftLeg.rotationPointY;
        this.leftFoot.rotationPointZ = this.bipedLeftLeg.rotationPointZ;
        this.leftFoot.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
        this.leftFoot.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
        this.leftFoot.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;

        this.rightFoot.rotationPointX = this.bipedRightLeg.rotationPointX;
        this.rightFoot.rotationPointY = this.bipedRightLeg.rotationPointY;
        this.rightFoot.rotationPointZ = this.bipedRightLeg.rotationPointZ;
        this.rightFoot.rotateAngleX = this.bipedRightLeg.rotateAngleX;
        this.rightFoot.rotateAngleY = this.bipedRightLeg.rotateAngleY;
        this.rightFoot.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;

        // Специальная обработка для зомби
        if (entity instanceof EntityZombie) {
            boolean armsRaised = false;
            if (entity instanceof EntityZombie)
                armsRaised = ((EntityZombie) entity).isArmsRaised();

            this.leftArm.rotateAngleY = (float) (8 * Math.PI / 180D);
            this.rightArm.rotateAngleY = -(float) (8 * Math.PI / 180D);
            if (armsRaised) {
                this.leftArm.rotateAngleX = -(float) (120 * Math.PI / 180D);
                this.rightArm.rotateAngleX = -(float) (120 * Math.PI / 180D);
            } else {
                this.leftArm.rotateAngleX = -(float) (80 * Math.PI / 180D);
                this.rightArm.rotateAngleX = -(float) (80 * Math.PI / 180D);
            }
        }

        // Обработка позиции при приседании
        if (this.isSneak) {
            this.head.offsetY = 4.24F;
            this.head.rotationPointY -= 1.045F;
            this.body.offsetY = 4F;
            this.rightArm.offsetY = 4F;
            this.leftArm.offsetY = 4F;
            this.rightFoot.offsetZ = this.rightLeg.offsetZ = 4F;
            this.leftFoot.offsetZ = this.leftLeg.offsetZ = 4F;

            this.rightFoot.rotationPointY = 12F;
            this.rightLeg.rotationPointY = 12F;
            this.leftFoot.rotationPointY = 12F;
            this.leftLeg.rotationPointY = 12F;

            this.rightFoot.rotationPointZ = -1F;
            this.rightLeg.rotationPointZ = -1F;
            this.leftFoot.rotationPointZ = -1F;
            this.leftLeg.rotationPointZ = -1F;

        } else {
            // Сброс смещений при нормальной стойке
            this.head.offsetY = 0F;
            this.body.offsetY = 0F;
            this.rightArm.offsetY = 0F;
            this.leftArm.offsetY = 0F;
            this.rightFoot.offsetZ = this.rightLeg.offsetZ = 0F;
            this.leftFoot.offsetZ = this.leftLeg.offsetZ = 0F;
        }
    }
}